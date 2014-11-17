package au.com.addstar.skyblock;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.entity.Player;

import au.com.addstar.skyblock.island.Coord;
import au.com.addstar.skyblock.island.Island;

public class IslandGrid
{
	private Island[][] mPlacements;
	private int mMinX;
	private int mMinZ;
	private int mMaxX;
	private int mMaxZ;
	
	public IslandGrid()
	{
		mPlacements = new Island[1][1];
		mMinX = mMaxX = 0;
		mMinZ = mMaxZ = 0;
	}
	
	public Island get(Coord coord)
	{
		return get(coord.getX(), coord.getZ());
	}
	
	public Island get(int x, int z)
	{
		if (x < mMinX || z < mMinZ)
			return null;
		
		x -= mMinX;
		z -= mMinZ;
		
		if (x >= mPlacements.length || z >= mPlacements[x].length)
			return null;
		
		return mPlacements[x][z];
	}
	
	public void set(Island island)
	{
		int x = island.getCoord().getX();
		int z = island.getCoord().getZ();
		
		resizeToInclude(x, z);
		
		x -= mMinX;
		z -= mMinZ;
		
		mPlacements[x][z] = island;
	}
	
	public void preload(int minX, int minZ, int maxX, int maxZ)
	{
		minX = Math.min(minX, mMinX);
		minZ = Math.min(minZ, mMinZ);
		maxX = Math.max(maxX, mMaxX);
		maxZ = Math.max(maxZ, mMaxZ);
		
		if (minX != mMinX || minZ != mMinZ || maxX != mMaxX || maxZ != mMaxZ)
			resize(minX, maxX, minZ, maxZ);
	}
	
	public void remove(Island island)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public Island createNew(Player player)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	public List<Island> getIslands()
	{
		LinkedList<Island> islands = new LinkedList<Island>();
		
		for (int x = 0; x < mPlacements.length; ++x)
		{
			for (int z = 0; z < mPlacements[x].length; ++z)
			{
				if (mPlacements[x][z] != null)
					islands.add(mPlacements[x][z]);
			}
		}
		
		return islands;
	}
	
	public Coord getMinExtent()
	{
		return new Coord(mMinX, mMinZ);
	}
	
	public Coord getMaxExtent()
	{
		return new Coord(mMaxX, mMaxZ);
	}
	
	private void resizeToInclude(int x, int z)
	{
		if (x < mMinX)
		{
			if (z < mMinZ)
				resize(x, mMaxX, z, mMaxZ);
			else if (z > mMaxZ)
				resize(x, mMaxX, mMinZ, z);
			else
				resize(x, mMaxX, mMinZ, mMaxZ);
		}
		else if (x > mMaxX)
		{
			if (z < mMinZ)
				resize(mMinX, x, z, mMaxZ);
			else if (z > mMaxZ)
				resize(mMinX, x, mMinZ, z);
			else
				resize(mMinX, x, mMinZ, mMaxZ);
		}
		else
		{
			if (z < mMinZ)
				resize(mMinX, mMaxX, z, mMaxZ);
			else if (z > mMaxZ)
				resize(mMinX, mMaxX, mMinZ, z);
		}
	}
	
	private void resize(int minX, int maxX, int minZ, int maxZ)
	{
		int width = maxX - minX;
		int height = maxZ - minZ;
		
		Island[][] newPlacements = new Island[width][];
		
		for (int x = 0; x < width; ++x)
		{
			newPlacements[x] = new Island[height];
			
			int xOffset = (minX - mMinX) + x;
			if (xOffset >= 0 && xOffset < mPlacements.length)
			{
				Island[] oldCol = mPlacements[xOffset];

				for (int z = 0; z < height; ++z)
				{
					int zOffset = (minZ - mMinZ) + z;
					if (xOffset >= 0 && xOffset < mPlacements.length)
						newPlacements[x][z] = oldCol[zOffset];
				}
			}
		}
		
		mPlacements = newPlacements;
		mMinX = minX;
		mMinZ = minZ;
		mMaxX = maxX;
		mMaxZ = maxZ;
	}
}
