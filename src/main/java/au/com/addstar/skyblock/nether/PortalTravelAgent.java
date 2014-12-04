package au.com.addstar.skyblock.nether;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TravelAgent;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import au.com.addstar.skyblock.island.Island;

public class PortalTravelAgent implements TravelAgent
{
	private TravelAgent mParent;
	private Island mIsland;
	
	public PortalTravelAgent(TravelAgent parent, Island island)
	{
		mParent = parent;
		mIsland = island;
	}
	
	@Override
	public boolean createPortal( Location location )
	{
		return mParent.createPortal(location);
	}

	@Override
	public Location findOrCreate( Location target )
	{
		Location portal = findPortal(target);
		if (portal == null)
		{
			if (createPortal(target))
				portal = findPortal(target);
			else
				portal = target;
		}
		return portal;
	}

	@Override
	public Location findPortal( Location location )
	{
		double bestDist = Double.MAX_VALUE;
		Location best = null;

		World world = location.getWorld();
		int minX = mIsland.getChunkCoord().getX() * 16;
		int minZ = mIsland.getChunkCoord().getZ() * 16;
		int maxX = minX + (mIsland.getWorld().getIslandChunkSize() * 16);
		int maxZ = minZ + (mIsland.getWorld().getIslandChunkSize() * 16);

		for (int x = minX; x < maxX; x++)
		{
			for (int z = minZ; z < maxZ; z++)
			{
				for (int y = world.getMaxHeight() - 1; y >= 0; --y)
				{
					Block block = world.getBlockAt(x, y, z);
					
					if (block.getType() == Material.PORTAL)
					{
						while (block.getRelative(BlockFace.DOWN).getType() == Material.PORTAL)
							block = block.getRelative(BlockFace.DOWN);
						
						double dist = block.getLocation().distanceSquared(location);

						if (dist < bestDist)
						{
							bestDist = dist;
							best = block.getLocation();
						}
					}
				}
			}
		}
		
		return best;
	}

	@Override
	public boolean getCanCreatePortal()
	{
		return true;
	}

	@Override
	public int getCreationRadius()
	{
		return 0;
	}

	@Override
	public int getSearchRadius()
	{
		return 0;
	}

	@Override
	public void setCanCreatePortal( boolean value )
	{
	}

	@Override
	public TravelAgent setCreationRadius( int radius )
	{
		return this;
	}

	@Override
	public TravelAgent setSearchRadius( int radius )
	{
		return this;
	}

}
