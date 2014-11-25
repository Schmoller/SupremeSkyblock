package au.com.addstar.skyblock;

import java.util.LinkedList;
import java.util.List;

import au.com.addstar.skyblock.island.Coord;
import au.com.addstar.skyblock.island.Island;

public class IslandGrid
{
	private Island[][] mPlacements;
	private int mMinX;
	private int mMinZ;
	private int mMaxX;
	private int mMaxZ;
	
	private int mIslandCount;
	
	public IslandGrid()
	{
		mPlacements = new Island[1][1];
		mMinX = mMaxX = 0;
		mMinZ = mMaxZ = 0;
		mIslandCount = 0;
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
		
		if (mPlacements[x][z] == null)
			++mIslandCount;
		
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
		int x = island.getCoord().getX();
		int z = island.getCoord().getZ();
		
		if (x < mMinX || z < mMinZ)
			return;
		
		x -= mMinX;
		z -= mMinZ;
		
		if (x >= mPlacements.length || z >= mPlacements[x].length)
			return;
		
		mPlacements[x][z] = null;
	}
	
	public Coord getNextEmpty()
	{
		// Spiral out in a clockwise direction looking for an empty slot
		int x = 0;
		int z = 0;
		
		int dir = 0;
		
		Island island = get(x, z);
		
		int initial = 0;
		
		while(island != null)
		{
			switch(dir)
			{
			case 0: // Next
				++x;
				++z;
				initial = x;
				
				++dir;
				break;
			case 1: // Right down
				--z;
				if (z <= -initial)
					++dir;
				break;
			case 2: // Bottom left
				--x;
				if (x <= -initial)
					++dir;
				break;
			case 3: // Left up
				++z;
				if (z >= initial)
					++dir;
				break;
			case 4: // Top right
				++x;
				if (x >= initial)
					dir = 0;
				break;
			}
			
			island = get(x,z);
		}
		
		return new Coord(x,z);
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
	
	public int getIslandCount()
	{
		return mIslandCount;
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
		int width = maxX - minX + 1;
		int height = maxZ - minZ + 1;
		
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
					if (zOffset >= 0 && zOffset < mPlacements[xOffset].length)
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
