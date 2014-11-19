package au.com.addstar.skyblock;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import au.com.addstar.skyblock.challenge.ChallengeManager;
import au.com.addstar.skyblock.challenge.rewards.ItemReward;
import au.com.addstar.skyblock.challenge.types.SubmitChallenge;
import au.com.addstar.skyblock.island.Island;
import au.com.addstar.skyblock.island.IslandTemplate;

public class SkyblockManager
{
	private HashMap<World, SkyblockWorld> mWorlds;
	private SkyblockPlugin mPlugin;
	private ChallengeManager mChallenges;
	
	private int mIslandChunkSize;
	private IslandTemplate mTemplate;
	
	public SkyblockManager(SkyblockPlugin plugin)
	{
		mPlugin = plugin;
		mWorlds = new HashMap<World, SkyblockWorld>();
		mChallenges = new ChallengeManager();
		
		// TODO: Remove this, it is just a test
		SubmitChallenge challenge = new SubmitChallenge("dirt2diamonds");
		challenge.getPrimaryRewards().add(new ItemReward(new ItemStack(Material.DIAMOND, 1)));
		challenge.getRequiredItems().add(new ItemStack(Material.DIRT, 10));
		challenge.setDescription(Arrays.asList("You discovered some magical force", "that allows you to transmute", "dirt into diamonds!"));
		mChallenges.addChallenge(challenge);
	}
	
	public void load(ConfigurationSection config)
	{
		loadSettings(config);
		loadWorlds(config);
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
}
