package au.com.addstar.skyblock.island;

import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;

import au.com.addstar.skyblock.SkyblockWorld;

public class Island
{
	private UUID mOwner;
	private final Coord mCoord;
	private final SkyblockWorld mWorld;
	private final Location mIslandOrigin;
	private Location mIslandSpawn;
	
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
		return mIslandSpawn.clone();
	}
	
	public void setIslandSpawn(Location spawn)
	{
		Validate.isTrue(spawn.getWorld().equals(mIslandOrigin.getWorld()));
		mIslandSpawn = spawn;
	}
	
}
