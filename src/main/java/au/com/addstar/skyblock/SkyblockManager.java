package au.com.addstar.skyblock;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;

import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;

import au.com.addstar.skyblock.challenge.ChallengeManager;
import au.com.addstar.skyblock.island.Island;
import au.com.addstar.skyblock.island.IslandTemplate;
import au.com.addstar.skyblock.misc.Utilities;
import au.com.addstar.skyblock.misc.ValueCallback;
import au.com.addstar.skyblock.vault.NullWrapper;
import au.com.addstar.skyblock.vault.IVault;
import au.com.addstar.skyblock.vault.VaultWrapper;

public class SkyblockManager
{
	private HashMap<World, SkyblockWorld> mWorlds;
	private TreeMultimap<Integer, Island> mTopIslands;
	private HashMap<Island, Integer> mTopReverse;
	
	private SkyblockPlugin mPlugin;
	private ChallengeManager mChallenges;
	private PointLookup mPointLookup;
	private ScoreUpdateSweep mScoreUpdater;
	private AbandonedIslandSweep mAbandonSweep;
	private SaveTask mSaver;
	private IVault mVault;
	
	// Config values
	
	private long mGeneralCleanupCutoff;
	private boolean mGeneralUseNether;
	
	private int mIslandChunkSize;
	private int mIslandHeight;
	private IslandTemplate mTemplate;
	private long mIslandRestartCooldown;
	private int mIslandMaxMembers;
	private int mIslandNeutralSize;
	
	private boolean mPlayerExcludeOwn;
	private int mPlayerMaxMembership;
	private boolean mPlayerReverseLava;
	private boolean mPlayerReverseWater;
	
	public SkyblockManager(SkyblockPlugin plugin)
	{
		mPlugin = plugin;
		mWorlds = new HashMap<World, SkyblockWorld>();
		mTopIslands = TreeMultimap.create(Ordering.natural().reverse(), Ordering.arbitrary());
		mTopReverse = new HashMap<Island, Integer>(); 
		
		mChallenges = new ChallengeManager(this);
		mPointLookup = new PointLookup();
		
		mScoreUpdater = new ScoreUpdateSweep(plugin);
		mAbandonSweep = new AbandonedIslandSweep(plugin);
	}
	
	public void init()
	{
		mScoreUpdater.start();
		mAbandonSweep.start();
		
		if (Bukkit.getPluginManager().isPluginEnabled("Vault"))
			mVault = new VaultWrapper();
		else
			mVault = new NullWrapper();
	}
	
	public void reload()
	{
		ConfigurationSection config = mPlugin.getConfig();
		loadSettings(config);
		mScoreUpdater.load(config);
		mAbandonSweep.load(config);
		
		mChallenges.loadChallenges(new File(mPlugin.getDataFolder(), "challenges.yml"));
		
		File pointFile = new File(mPlugin.getDataFolder(), "points.yml");
		if (!pointFile.exists())
			mPlugin.saveResource("points.yml", false);
		
		mPointLookup.load(pointFile, mPlugin.getLogger());
	}
	
	public void load()
	{
		reload();
		
		ConfigurationSection config = mPlugin.getConfig();
		loadWorlds(config);
		loadTopList();
	}
	
	private void loadWorlds(ConfigurationSection config)
	{
		File worldDir = new File(mPlugin.getDataFolder(), "worlds");
		
		if (!worldDir.exists())
			worldDir.mkdirs();
		
		mWorlds.clear();
		
		List<String> worldNames = config.getStringList("worlds");
		for(String name : worldNames)
		{
			name = name.toLowerCase();
			if (mWorlds.containsKey(name))
			{
				mPlugin.getLogger().warning("The world '" + name + "' is already defined as a skyblock world");
				continue;
			}
			
			// Dont process existing worlds
			if (Bukkit.getWorld(name) != null)
				continue;
			
			SkyblockWorld world = new SkyblockWorld(name, this);
			
			if (world.load())
			{
				for (Environment env : Environment.values())
				{
					if (world.getWorld(env) != null)
						mWorlds.put(world.getWorld(env), world);
				}
			}
		}
	}
	
	private void loadSettings(ConfigurationSection config)
	{
		// Load island options
		ConfigurationSection island = config.getConfigurationSection("island");
		mIslandChunkSize = island.getInt("size", 4);
		if (mIslandChunkSize <= 0)
			mIslandChunkSize = 4;
		
		mIslandHeight  = island.getInt("height", 190);
		if (mIslandHeight < 10)
			mIslandHeight = 190;
		
		String templateName = island.getString("template", "original");
		
		mIslandRestartCooldown = Utilities.parseTimeDiffSafe(island.getString("restart-cooldown", "1d"), TimeUnit.DAYS.toMillis(1), mPlugin.getLogger());
		
		mIslandMaxMembers = island.getInt("max-members", -1);
		mIslandNeutralSize = island.getInt("neutral-zone-size", 4);
		if (mIslandNeutralSize < 0)
			mIslandNeutralSize = 4;
		
		mTemplate = load(templateName);
		
		// Load player options
		ConfigurationSection player = config.getConfigurationSection("player");
		mPlayerExcludeOwn = player.getBoolean("exclude-own", true);
		mPlayerMaxMembership = player.getInt("max-membership", 1);
		if (mPlayerMaxMembership < 0)
			mPlayerMaxMembership = 1;
		
		mPlayerReverseLava = player.getBoolean("reverse.lava");
		mPlayerReverseWater = player.getBoolean("reverse.water");
		
		// Load general options
		long saveInterval = Utilities.parseTimeDiffSafe(config.getString("general.save-interval", "5m"), TimeUnit.MINUTES.toMillis(5), mPlugin.getLogger());
		saveInterval /= 50;
		
		if (mSaver != null)
			mSaver.cancel();
		
		mSaver = new SaveTask(this);
		mSaver.runTaskTimer(mPlugin, saveInterval, saveInterval);
		
		mGeneralCleanupCutoff = Utilities.parseTimeDiffSafe(config.getString("general.cleanup-cutoff", "4mo"), TimeUnit.DAYS.toMillis(520), mPlugin.getLogger());
		
		mGeneralUseNether = config.getBoolean("general.use-nether", true);
	}
	
	private IslandTemplate load(String name)
	{
		File file = new File(mPlugin.getDataFolder(), "templates/" + name + ".template");
		IslandTemplate template = new IslandTemplate();
		
		// Try file system
		if (file.exists() && template.load(file))
			return template;
		
		// Try jar
		InputStream stream = mPlugin.getResource("templates/" + name + ".template");
		if (stream != null && template.load(stream))
			return template;
		
		// Fallback
		if (!name.equals("original"))
			return load("original");
		
		throw new IllegalStateException("The default island template is missing! Unable to load");
	}
	
	private void loadTopList()
	{
		mTopIslands.clear();
		mTopReverse.clear();
		
		File file = new File(mPlugin.getDataFolder(), "ranked.dat");
		if (!file.exists())
			return;
		
		DataInputStream in = null;
		
		try
		{
			in = new DataInputStream(new FileInputStream(file));
			
			int count = in.readInt();
			for (int i = 0; i < count; ++i)
			{
				UUID id = new UUID(in.readLong(), in.readLong());
				int score = in.readInt();
				
				Island island = getIsland(id);
				if (island != null)
				{
					mTopIslands.put(score, island);
					mTopReverse.put(island, score);
				}
			}
		}
		catch(IOException e)
		{
			mPlugin.getLogger().severe("Unable to load the ranked islands list (ranked.dat). Ranks will have to be rebuilt");
		}
		finally
		{
			Utilities.close(in, mPlugin.getLogger());
		}
	}
	
	public void save()
	{
		for(SkyblockWorld world : mWorlds.values())
			world.save();
		saveTopList();
	}
	
	private void saveTopList()
	{
		File file = new File(mPlugin.getDataFolder(), "ranked.dat");
		
		DataOutputStream out = null;
		try
		{
			out = new DataOutputStream(new FileOutputStream(file));
			
			out.writeInt(mTopIslands.size());
			
			for (Entry<Integer, Island> island : mTopIslands.entries())
			{
				out.writeLong(island.getValue().getOwner().getMostSignificantBits());
				out.writeLong(island.getValue().getOwner().getLeastSignificantBits());
				
				out.writeInt(island.getKey());
			}
		}
		catch(IOException e)
		{
			mPlugin.getLogger().severe("Unable to save the ranked islands list. Ranks will have to be rebuilt next start");
		}
		finally
		{
			Utilities.close(out, mPlugin.getLogger());
		}
	}
	
	public SkyblockWorld getNextSkyblockWorld()
	{
		SkyblockWorld best = null;
		for (SkyblockWorld world : mWorlds.values())
		{
			if (best == null || world.getGrid().getIslandCount() < best.getGrid().getIslandCount())
				best = world;
		}
		
		return best;
	}
	
	public SkyblockWorld getSkyblockWorld(World world)
	{
		return mWorlds.get(world);
	}
	
	public Island getIsland(UUID owner)
	{
		for (SkyblockWorld world : mWorlds.values())
		{
			Island is = world.getIsland(owner);
			if (is != null)
				return is;
		}
		
		return null;
	}
	
	public Set<Island> getIslands(UUID player)
	{
		Set<Island> islands = null;
		for (SkyblockWorld world : mWorlds.values())
		{
			Set<Island> worldIslands = world.getIslands(player);
			if (!worldIslands.isEmpty())
			{
				if (islands == null)
					islands = worldIslands;
				else
					islands = Sets.union(islands, worldIslands);
			}
		}
		
		if (islands == null)
			return Collections.emptySet();
		else
			return islands;
	}
	
	public Island getIslandAt(Location location)
	{
		SkyblockWorld world = mWorlds.get(location.getWorld());
		if (world == null)
			return null;
		
		return world.getIslandAt(location);
	}
	
	public Island getIslandAt(Location location, boolean includeNeutralZone)
	{
		SkyblockWorld world = mWorlds.get(location.getWorld());
		if (world == null)
			return null;
		
		return world.getIslandAt(location, includeNeutralZone);
	}
	
	public void queueScoreUpdate(Island island)
	{
		mScoreUpdater.queueIsland(island);
	}
	
	public void queueAbandoned(Island island)
	{
		mAbandonSweep.queueIsland(island);
	}
	
	public void updateRank(Island island)
	{
		Integer oldScore = mTopReverse.remove(island);
		if (oldScore != null)
			mTopIslands.remove(oldScore, island);
		
		mTopIslands.put(island.getScore(), island);
		mTopReverse.put(island, island.getScore());
	}
	
	public int getRank(Island island)
	{
		int rank = 1;
		for (Island other : mTopIslands.values())
		{
			if (island == other)
				return rank;
			
			++rank;
		}
		
		// Unranked
		return -1;
	}
	
	public Set<Entry<Integer, Island>> getTopScores()
	{
		return Collections.unmodifiableSet(mTopIslands.entries());
	}
	
	public void runCleanup(long cutoffDate, ValueCallback<Integer> callback)
	{
		new PurgeTask(cutoffDate, mWorlds.values(), callback).runTaskTimer(mPlugin, 1, 1);
	}
	
	void removeIsland(Island island)
	{
		Integer score = mTopReverse.remove(island);
		if (score != null)
			mTopIslands.remove(score, island);
	}
	
	public int getIslandChunkSize()
	{
		return mIslandChunkSize;
	}
	
	public int getIslandHeight()
	{
		return mIslandHeight;
	}
	
	public long getIslandRestartCooldown()
	{
		return mIslandRestartCooldown;
	}
	
	public IslandTemplate getTemplate()
	{
		return mTemplate;
	}
	
	public int getIslandMaxMembers()
	{
		return mIslandMaxMembers;
	}
	
	public int getIslandNeutralSize()
	{
		return mIslandNeutralSize;
	}
	
	public boolean getPlayerExcludeOwn()
	{
		return mPlayerExcludeOwn;
	}
	
	public int getPlayerMaxMembership()
	{
		return mPlayerMaxMembership;
	}
	
	public boolean getPlayerCanReverseLava()
	{
		return mPlayerReverseLava;
	}
	
	public boolean getPlayerCanReverseWater()
	{
		return mPlayerReverseWater;
	}
	
	public long getCleanupCutoff()
	{
		return mGeneralCleanupCutoff;
	}
	
	public boolean getUsesNether()
	{
		return mGeneralUseNether;
	}
	
	public File getWorldFolder(String world)
	{
		return new File(mPlugin.getDataFolder(), "worlds/" + world);
	}
	
	public SkyblockPlugin getPlugin()
	{
		return mPlugin;
	}
	
	public ChallengeManager getChallenges()
	{
		return mChallenges;
	}
	
	public PointLookup getPointLookup()
	{
		return mPointLookup;
	}
	
	public IVault getVault()
	{
		return mVault;
	}
}
