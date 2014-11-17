package au.com.addstar.skyblock;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import au.com.addstar.skyblock.island.Coord;
import au.com.addstar.skyblock.island.Island;

public class SkyblockWorld
{
	private String mName;
	private World mWorld;
	private SkyblockManager mManager;
	private IslandGrid mGrid;
	
	public SkyblockWorld(String name, SkyblockManager manager)
	{
		mManager = manager;
		mName = name;
		mGrid = new IslandGrid();
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
	
	public boolean load()
	{
		try
		{
			loadWorld();
			loadIslands();
			
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
	
	private void loadIslands() throws IOException, InvalidConfigurationException
	{
		File worldDir = mManager.getWorldFolder(mName);
		File islandsFile = new File(worldDir, "islands.yml");
		
		YamlConfiguration islandsConfig = new YamlConfiguration();
		islandsConfig.load(islandsFile);
		
		mGrid = new IslandGrid();
		mGrid.preload(islandsConfig.getInt("bounds.x.min", 0), islandsConfig.getInt("bounds.z.min", 0), islandsConfig.getInt("bounds.x.max", 0), islandsConfig.getInt("bounds.z.max", 0));
		
		// Load island occupancies
		ConfigurationSection section = islandsConfig.getConfigurationSection("islands");
		for (String key : section.getKeys(false))
		{
			String[] strCoords = key.split("_");
			Coord coords = new Coord(Integer.parseInt(strCoords[0]), Integer.parseInt(strCoords[1]));
			UUID owner = UUID.fromString(section.getString(key));
			
			mGrid.set(new Island(owner, coords));
		}
	}
	
	public void save()
	{
		File worldDir = mManager.getWorldFolder(mName);
		try
		{
			if (!worldDir.exists())
				worldDir.mkdirs();
			
			saveIslands(worldDir);
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
			section.set(String.format("%d_%d", island.getCoord().getX(), island.getCoord().getZ()), island.getOwner().toString());
		
		islandsConfig.save(islandsFile);
	}
}
