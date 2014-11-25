package au.com.addstar.skyblock;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import net.minecraft.util.org.apache.commons.lang3.Validate;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.google.common.base.Functions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;

import au.com.addstar.skyblock.island.Coord;
import au.com.addstar.skyblock.island.Island;
import au.com.addstar.skyblock.island.IslandTemplate;
import au.com.addstar.skyblock.misc.Utilities;

public class SkyblockWorld
{
	private String mName;
	private World mWorld;
	private SkyblockManager mManager;
	private IslandGrid mGrid;
	
	private int mIslandChunkSize;
	private int mIslandHeight;
	
	private SetMultimap<UUID, Island> mOwnerMap;
	
	public SkyblockWorld(String name, SkyblockManager manager)
	{
		mManager = manager;
		mName = name;
		mGrid = new IslandGrid();
		mOwnerMap = HashMultimap.create();
		
		mIslandChunkSize = manager.getIslandChunkSize();
		mIslandHeight = manager.getIslandHeight();
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
	
	public int getIslandHeight()
	{
		return mIslandHeight;
	}
	
	public Island createIsland(Player player)
	{
		Coord coords = mGrid.getNextEmpty();
		Island island = new Island(player.getUniqueId(), Collections.<UUID>emptyList(), coords, this);
		
		// Assign the space
		mGrid.set(island);
		mOwnerMap.put(player.getUniqueId(), island);

		IslandTemplate template = mManager.getTemplate();
		
		// Configure the island
		island.setIslandSpawn(template.getSpawnLocation(island.getIslandOrigin()));
		island.setOwnerName(player.getDisplayName());
		
		// Place it
		island.placeIsland();
		
		return island;
	}
	
	public void removeIsland(Island island)
	{
		mManager.removeIsland(island);
		for (UUID member : island.getMembers())
			mOwnerMap.remove(member, island);
		mOwnerMap.remove(island.getOwner(), island);
		mGrid.remove(island);
		mManager.removeIsland(island);
		
		for (Player player : Utilities.getPlayersOnIsland(island))
			Utilities.sendPlayerHome(player);
		
		island.clear();
		
		File base = new File(mManager.getWorldFolder(mWorld.getName()), "islands");
		File file = new File(base, String.format("%d,%d", island.getCoord().getX(), island.getCoord().getZ()));
		if (file.exists())
			file.delete();
	}
	
	public void updateIslandMembership(Island island, UUID player)
	{
		if (island.getMembers().contains(player))
			mOwnerMap.put(player, island);
		else
			mOwnerMap.remove(player, island);
	}
	
	public Island getIsland(UUID owner)
	{
		for (Island island : mOwnerMap.get(owner))
		{
			if (island.getOwner().equals(owner))
				return island;
		}
		return null;
	}
	
	public Set<Island> getIslands(UUID player)
	{
		return mOwnerMap.get(player);
	}
	
	public Island getIslandAt(Location location)
	{
		return getIslandAt(location, true);
	}
	
	public Island getIslandAt(Location location, boolean includeNeutralZone)
	{
		Validate.isTrue(location.getWorld().equals(mWorld));
		
		// Chunk coords
		int x = location.getBlockX() >> 4;
		int z = location.getBlockZ() >> 4;
		
		// island coords
		x = (int)Math.floor(x / (float)mIslandChunkSize);
		z = (int)Math.floor(z / (float)mIslandChunkSize);
		
		Island island = mGrid.get(x, z);
		
		// Check if in neutral zone
		if (includeNeutralZone && island != null)
		{
			Coord min = island.getChunkCoord();
			int minX = min.getX() * 16 + mManager.getIslandNeutralSize();
			int minZ = min.getZ() * 16 + mManager.getIslandNeutralSize();
			
			int maxX = (min.getX() + getIslandChunkSize()) * 16 - mManager.getIslandNeutralSize();
			int maxZ = (min.getZ() + getIslandChunkSize()) * 16 - mManager.getIslandNeutralSize();
			
			if (location.getBlockX() < minX || location.getBlockX() >= maxX || location.getBlockZ() < minZ || location.getBlockZ() >= maxZ)
				// Inside neutral zone
				return null;
		}
		
		return island;
	}
	
	public SkyblockManager getManager()
	{
		return mManager;
	}
	
	public void updateOwner(Island island, UUID oldOwner)
	{
		if (!island.getMembers().contains(oldOwner))
			mOwnerMap.remove(oldOwner, island);
		
		if (!island.getOwner().equals(Utilities.nobody))
			mOwnerMap.put(island.getOwner(), island);
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
			
			UUID owner;
			List<UUID> members;
			if (section.isString(key))
			{
				owner = UUID.fromString(section.getString(key));
				members = Collections.emptyList();
			}
			else
			{
				List<String> ids = section.getStringList(key);
				owner = UUID.fromString(ids.get(0));
				
				members = new ArrayList<UUID>(ids.size()-1);
				for (String id : Iterables.skip(ids, 1))
					members.add(UUID.fromString(id));
			}
			
			Island island = new Island(owner, members, coords, this);
			
			if (owner.equals(Utilities.nobody))
				mManager.queueAbandoned(island);
			else
			{
				mOwnerMap.put(owner, island);
				for (UUID member : members)
					mOwnerMap.put(member, island);
			}
			
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
		mIslandHeight = settingsConfig.getInt("height", mIslandHeight);
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
			if (island.getMembers().isEmpty())
				section.set(String.format("%d_%d", island.getCoord().getX(), island.getCoord().getZ()), island.getOwner().toString());
			else
			{
				List<UUID> base = Lists.newArrayList(Iterables.concat(Lists.newArrayList(island.getOwner()), island.getMembers()));
				List<String> ids = Lists.transform(base, Functions.toStringFunction());
				section.set(String.format("%d_%d", island.getCoord().getX(), island.getCoord().getZ()), ids);
			}
			island.saveIfNeeded();
		}
		
		islandsConfig.save(islandsFile);
	}
	
	private void saveSettings(File worldDir) throws IOException
	{
		File settingsFile = new File(worldDir, "settings.yml");
		
		YamlConfiguration settingsConfig = new YamlConfiguration();
		
		settingsConfig.set("size", mIslandChunkSize);
		settingsConfig.set("height", mIslandHeight);
		
		settingsConfig.options().header("WARNING! This file is automatically generated. Do NOT change any values here.\nChanges to this file may permanently damage this skyblock world");
		settingsConfig.save(settingsFile);
	}
}
