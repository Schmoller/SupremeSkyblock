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
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import com.google.common.io.Closeables;

import au.com.addstar.skyblock.challenge.ChallengeManager;
import au.com.addstar.skyblock.island.Island;
import au.com.addstar.skyblock.island.IslandTemplate;

public class SkyblockManager
{
	private HashMap<World, SkyblockWorld> mWorlds;
	private TreeMultimap<Integer, Island> mTopIslands;
	private HashMap<Island, Integer> mTopReverse;
	
	private SkyblockPlugin mPlugin;
	private ChallengeManager mChallenges;
	private PointLookup mPointLookup;
	private ScoreUpdateSweep mScoreUpdater;
	private SaveTask mSaver;
	
	private int mIslandChunkSize;
	private IslandTemplate mTemplate;
	
	public SkyblockManager(SkyblockPlugin plugin)
	{
		mPlugin = plugin;
		mWorlds = new HashMap<World, SkyblockWorld>();
		mTopIslands = TreeMultimap.create(Ordering.natural().reverse(), Ordering.arbitrary());
		mTopReverse = new HashMap<Island, Integer>(); 
		
		mChallenges = new ChallengeManager(this);
		mPointLookup = new PointLookup();
		
		mScoreUpdater = new ScoreUpdateSweep(plugin);
	}
	
	public void init()
	{
		mScoreUpdater.start();
	}
	
	public void load(ConfigurationSection config)
	{
		loadSettings(config);
		loadWorlds(config);
		mScoreUpdater.load(config);
		loadTopList();
		
		mChallenges.loadChallenges(new File(mPlugin.getDataFolder(), "challenges.yml"));
		
		File pointFile = new File(mPlugin.getDataFolder(), "points.yml");
		if (!pointFile.exists())
			mPlugin.saveResource("points.yml", false);
		
		mPointLookup.load(pointFile, mPlugin.getLogger());
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
				mWorlds.put(world.getWorld(), world);
		}
	}
	
	private void loadSettings(ConfigurationSection config)
	{
		// Load other options
		ConfigurationSection island = config.getConfigurationSection("island");
		mIslandChunkSize = island.getInt("size", 4);
		if (mIslandChunkSize <= 0)
			mIslandChunkSize = 4;
		
		String templateName = island.getString("template", "original");
		
		mTemplate = load(templateName);
		
		int saveInterval = config.getInt("general.save-interval", 6000);
		if (saveInterval <= 0)
			saveInterval = 6000;
		
		if (mSaver != null)
			mSaver.cancel();
		
		mSaver = new SaveTask(this);
		mSaver.runTaskTimer(mPlugin, saveInterval, saveInterval);
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
			Closeables.closeQuietly(in);
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
			Closeables.closeQuietly(out);
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
	
	public Island getIslandAt(Location location)
	{
		SkyblockWorld world = mWorlds.get(location.getWorld());
		if (world == null)
			return null;
		
		return world.getIslandAt(location);
	}
	
	public void queueScoreUpdate(Island island)
	{
		mScoreUpdater.queueIsland(island);
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
	
	public int getIslandChunkSize()
	{
		return mIslandChunkSize;
	}
	
	public IslandTemplate getTemplate()
	{
		return mTemplate;
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
}
