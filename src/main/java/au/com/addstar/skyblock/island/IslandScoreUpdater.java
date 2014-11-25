package au.com.addstar.skyblock.island;

import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import au.com.addstar.skyblock.PointLookup;
import au.com.addstar.skyblock.challenge.Challenge;

public class IslandScoreUpdater extends BukkitRunnable
{
	private Location mMinBlock;
	private Location mMaxBlock;
	private Island mIsland;
	private long mMaxIterationTime;
	
	private int mX;
	private int mY;
	private int mZ;
	
	private int mScore;
	
	private PointLookup mLookup;
	private World mWorld;
	
	private int[] mCounts = new int[Material.values().length];
	
	private boolean mIsRunning;
	
	public IslandScoreUpdater(Island island, int timeLimit)
	{
		mIsland = island;
		
		mMinBlock = new Location(island.getWorld().getWorld(), island.getChunkCoord().getX() * 16 + island.getWorld().getManager().getIslandNeutralSize(), 0, island.getChunkCoord().getZ() * 16 + island.getWorld().getManager().getIslandNeutralSize());
		mMaxBlock = new Location(island.getWorld().getWorld(), (island.getChunkCoord().getX() + island.getWorld().getIslandChunkSize()) * 16 - island.getWorld().getManager().getIslandNeutralSize() - 1, island.getWorld().getWorld().getMaxHeight(), (island.getChunkCoord().getZ() + island.getWorld().getIslandChunkSize()) * 16 - island.getWorld().getManager().getIslandNeutralSize() - 1);
		
		mMaxIterationTime = TimeUnit.NANOSECONDS.convert(timeLimit, TimeUnit.MILLISECONDS);
		
		mX = mMinBlock.getBlockX() - 1;
		mY = mMinBlock.getBlockY();
		mZ = mMinBlock.getBlockZ();
		
		mWorld = mMinBlock.getWorld();
		mLookup = mIsland.getWorld().getManager().getPointLookup();
		
		mScore = 0;
		
		mIsRunning = true;
	}
	
	/**
	 * This is a time divided task, it should not be called directly, only through the runTaskTimer() method
	 */
	public void run()
	{
		long time = System.nanoTime();
		while(true)
		{
			if(System.nanoTime() - time >= mMaxIterationTime)
				return;
		
			// Manual iteration
			if (mX < mMaxBlock.getBlockX())
				++mX;
			else
			{
				mX = mMinBlock.getBlockX();
				if (mZ < mMaxBlock.getBlockZ())
					++mZ;
				else
				{
					mZ = mMinBlock.getBlockZ();
					if (mY < mMaxBlock.getBlockY())
						++mY;
					else
						break;
				}
			}
			
			// Calculate score
			Block block = mWorld.getBlockAt(mX, mY, mZ);
			if (block.isEmpty())
				continue;
			
			int limit = mLookup.getLimit(block);
			if (limit >= 0)
			{
				if (mCounts[block.getType().ordinal()]++ >= limit)
					continue;
			}
			
			mScore += mLookup.getScore(block);
		}
		
		// Add the points for completed challenges
		for (Challenge challenge : mIsland.getWorld().getManager().getChallenges().getChallenges())
		{
			if (mIsland.getChallengeStorage().isComplete(challenge))
				mScore += challenge.getPointReward();
		}
		
		mIsland.setScore(mScore);
		cancel();
		mIsRunning = false;
	}
	
	public boolean isRunning()
	{
		return mIsRunning;
	}
}
