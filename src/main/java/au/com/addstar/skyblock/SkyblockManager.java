package au.com.addstar.skyblock;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class SkyblockManager
{
	private HashMap<World, SkyblockWorld> mWorlds;
	private SkyblockPlugin mPlugin;
	
	private int mIslandChunkSize;
	
	public SkyblockManager(SkyblockPlugin plugin)
	{
		mPlugin = plugin;
		mWorlds = new HashMap<World, SkyblockWorld>();
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
	
	public int getIslandChunkSize()
	{
		return mIslandChunkSize;
	}
	
	public File getWorldFolder(String world)
	{
		return new File(mPlugin.getDataFolder(), "worlds/" + world);
	}
	
	public SkyblockPlugin getPlugin()
	{
		return mPlugin;
	}
}
