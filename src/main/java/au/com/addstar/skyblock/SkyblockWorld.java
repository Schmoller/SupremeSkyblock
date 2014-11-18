package au.com.addstar.skyblock;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import au.com.addstar.skyblock.island.Coord;
import au.com.addstar.skyblock.island.Island;
import au.com.addstar.skyblock.island.IslandTemplate;

public class SkyblockWorld
{
	private String mName;
	private World mWorld;
	private SkyblockManager mManager;
	private IslandGrid mGrid;
	
	private int mIslandChunkSize;
	
	private HashMap<UUID, Island> mOwnerMap;
	
	public SkyblockWorld(String name, SkyblockManager manager)
	{
		mManager = manager;
		mName = name;
		mGrid = new IslandGrid();
		mOwnerMap = new HashMap<UUID, Island>();
		
		mIslandChunkSize = manager.getIslandChunkSize();
	}
	
	public String getName()
	{
		return mName;
	}
	
	public World getWorld()
	{
		return mWorld;
	}
	
	public IslandGrid getGrid()
	{
		return mGrid;
	}
	
	public int getIslandChunkSize()
	{
		return mIslandChunkSize;
	}
	
	public Island createIsland(Player player)
	{
		Coord coords = mGrid.getNextEmpty();
		Island island = new Island(player.getUniqueId(), coords, this);
		
		// Assign the space
		mGrid.set(island);
		mOwnerMap.put(player.getUniqueId(), island);

		IslandTemplate template = mManager.getTemplate();
		
		// Configure the island
		Location loc = island.getIslandOrigin();
		island.setIslandSpawn(template.getSpawnLocation(loc));
		
		// Place it
		template.placeAt(loc);
		
		return island;
	}
	
	public Island getIsland(UUID owner)
	{
		return mOwnerMap.get(owner);
	}
	
	public SkyblockManager getManager()
	{
		return mManager;
	}
	
	public boolean load()
	{
		try
		{
			File worldDir = mManager.getWorldFolder(mName);
			
			loadWorld();
			loadIslands(worldDir);
			loadSettings(worldDir);
			
			return true;
		}
		catch(IOException e)
		{
			mManager.getPlugin().getLogger().log(Level.SEVERE, "Error loading skyblock world '" + mName + "': ", e);
			return false;
		}
		catch(InvalidConfigurationException e)
		{
			mManager.getPlugin().getLogger().log(Level.SEVERE, "Error loading skyblock world '" + mName + "': ", e);
			return false;
		}
	}
	
	private void loadWorld()
	{
		mWorld = new WorldCreator(mName)
		.environment(Environment.NORMAL)
		.generateStructures(false)
		.generator(new EmptyGenerator())
		.type(WorldType.FLAT)
		.seed(0)
		.createWorld();
	}
	
	private void loadIslands(File worldDir) throws IOException, InvalidConfigurationException
	{
		File islandsFile = new File(worldDir, "islands.yml");
		
		YamlConfiguration islandsConfig = new YamlConfiguration();
		mGrid = new IslandGrid();
		
		if (islandsFile.exists())
			islandsConfig.load(islandsFile);
		else
			return;
		
		mGrid.preload(islandsConfig.getInt("bounds.x.min", 0), islandsConfig.getInt("bounds.z.min", 0), islandsConfig.getInt("bounds.x.max", 0), islandsConfig.getInt("bounds.z.max", 0));
		
		// Load island occupancies
		ConfigurationSection section = islandsConfig.getConfigurationSection("islands");
		for (String key : section.getKeys(false))
		{
			String[] strCoords = key.split("_");
			Coord coords = new Coord(Integer.parseInt(strCoords[0]), Integer.parseInt(strCoords[1]));
			UUID owner = UUID.fromString(section.getString(key));
			
			Island island = new Island(owner, coords, this);
			mOwnerMap.put(owner, island);
			mGrid.set(island);
		}
	}
	
	private void loadSettings(File worldDir) throws IOException, InvalidConfigurationException
	{
		File settingsFile = new File(worldDir, "settings.yml");
		
		YamlConfiguration settingsConfig = new YamlConfiguration();
		if (settingsFile.exists())
			settingsConfig.load(settingsFile);
		
		mIslandChunkSize = settingsConfig.getInt("size", mIslandChunkSize);
	}
	
	public void save()
	{
		File worldDir = mManager.getWorldFolder(mName);
		try
		{
			if (!worldDir.exists())
				worldDir.mkdirs();
			
			saveIslands(worldDir);
			saveSettings(worldDir);
		}
		catch(IOException e)
		{
			mManager.getPlugin().getLogger().log(Level.SEVERE, "Error saving skyblock world '" + mName + "': ", e);
		}
	}
	
	private void saveIslands(File worldDir) throws IOException
	{
		List<Island> islands = mGrid.getIslands();
		
		File islandsFile = new File(worldDir, "islands.yml");
		
		YamlConfiguration islandsConfig = new YamlConfiguration();
		
		// Record the bounds information
		Coord min = mGrid.getMinExtent();
		Coord max = mGrid.getMaxExtent();
		islandsConfig.set("bounds.x.min", min.getX());
		islandsConfig.set("bounds.z.min", min.getZ());
		
		islandsConfig.set("bounds.x.max", max.getX());
		islandsConfig.set("bounds.z.max", max.getZ());
		
		// Record island occupancies
		ConfigurationSection section = islandsConfig.createSection("islands");
		for(Island island : islands)
		{
			section.set(String.format("%d_%d", island.getCoord().getX(), island.getCoord().getZ()), island.getOwner().toString());
			island.saveIfNeeded();
		}
		
		islandsConfig.save(islandsFile);
	}
	
	private void saveSettings(File worldDir) throws IOException
	{
		File settingsFile = new File(worldDir, "settings.yml");
		
		YamlConfiguration settingsConfig = new YamlConfiguration();
		
		settingsConfig.set("size", mIslandChunkSize);
		
		settingsConfig.options().header("WARNING! This file is automatically generated. Do NOT change any values here.\nChanges to this file may permanently damage this skyblock world");
		settingsConfig.save(settingsFile);
	}
}
