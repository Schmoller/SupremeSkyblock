package au.com.addstar.skyblock.island;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import au.com.addstar.skyblock.SkyblockWorld;

public class Island
{
	private UUID mOwner;
	private final Coord mCoord;
	private final SkyblockWorld mWorld;
	private final Location mIslandOrigin;
	private Location mIslandSpawn;
	
	private boolean mHasLoaded = false;
	private boolean mIsModified = false;
	
	public Island(UUID owner, Coord coords, SkyblockWorld world)
	{
		mOwner = owner;
		mCoord = coords;
		mWorld = world;

		Coord chunkMin = getChunkCoord();
		int halfSize = (mWorld.getIslandChunkSize() * 16) / 2;
		
		mIslandOrigin = new Location(mWorld.getWorld(), chunkMin.getX() * 16 + halfSize, 190, chunkMin.getZ() * 16 + halfSize);
	}
	
	public UUID getOwner()
	{
		return mOwner;
	}
	
	public Coord getCoord()
	{
		return mCoord;
	}
	
	public SkyblockWorld getWorld()
	{
		return mWorld;
	}
	
	public Coord getChunkCoord()
	{
		return new Coord(mCoord.getX() * mWorld.getIslandChunkSize(), mCoord.getZ() * mWorld.getIslandChunkSize());
	}
	
	public Location getIslandOrigin()
	{
		return mIslandOrigin.clone();
	}
	
	public Location getIslandSpawn()
	{
		loadIfNeeded();
		
		return mIslandSpawn.clone();
	}
	
	public void setIslandSpawn(Location spawn)
	{
		Validate.isTrue(spawn.getWorld().equals(mIslandOrigin.getWorld()));
		mIslandSpawn = spawn;
		
		mIsModified = true;
	}
	
	public void saveIfNeeded()
	{
		if (mIsModified)
			save();
	}
	
	public void save()
	{
		try
		{
			YamlConfiguration config = new YamlConfiguration();
			config.options().header("WARNING! This file is automatically generated. Do not edit this file");
			
			File base = new File(mWorld.getManager().getWorldFolder(mWorld.getName()), "islands");
			if (!base.exists())
				base.mkdirs();
			
			save(config);
			
			config.save(new File(base, mOwner.toString()));
		}
		catch(IOException e)
		{
			mWorld.getManager().getPlugin().getLogger().warning("Failed to save island for " + mOwner);
			e.printStackTrace();
		}
		
		mHasLoaded = true;
		mIsModified = false;
	}
	
	private void save(ConfigurationSection dest)
	{
		if (mIslandSpawn != null)
		{
			ConfigurationSection spawn = dest.createSection("spawn");
			spawn.set("x", mIslandSpawn.getX());
			spawn.set("y", mIslandSpawn.getY());
			spawn.set("z", mIslandSpawn.getZ());
			spawn.set("yaw", mIslandSpawn.getYaw());
			spawn.set("pitch", mIslandSpawn.getPitch());
		}
	}
	
	public void loadIfNeeded()
	{
		if (!mHasLoaded)
			load();
	}
	
	public void load()
	{
		File base = new File(mWorld.getManager().getWorldFolder(mWorld.getName()), "islands");
		File islandFile = new File(base, mOwner.toString());
		
		if (!islandFile.exists())
			return;
		
		try
		{
			YamlConfiguration config = new YamlConfiguration();
			config.load(islandFile);
			
			load(config);
		}
		catch(IOException e)
		{
			mWorld.getManager().getPlugin().getLogger().warning("Failed to load island for " + mOwner);
			e.printStackTrace();
		}
		catch(InvalidConfigurationException e)
		{
			mWorld.getManager().getPlugin().getLogger().warning("Failed to load island for " + mOwner);
			e.printStackTrace();
		}
		
		mHasLoaded = true;
		mIsModified = false;
	}
	
	private void load(ConfigurationSection source)
	{
		if (source.isConfigurationSection("spawn"))
		{
			ConfigurationSection spawn = source.getConfigurationSection("spawn");
			mIslandSpawn = new Location(mWorld.getWorld(), spawn.getDouble("x"), spawn.getDouble("y"), spawn.getDouble("z"), (float)spawn.getDouble("yaw"), (float)spawn.getDouble("pitch"));
		}
	}
}
