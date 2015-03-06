package au.com.addstar.skyblock.island;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockVector;

import com.google.common.collect.Iterators;

import au.com.addstar.skyblock.PointLookup;
import au.com.addstar.skyblock.SkyblockWorld;
import au.com.addstar.skyblock.challenge.Challenge;
import au.com.addstar.skyblock.challenge.IslandBasedChallenge;

public class IslandScoreUpdater extends BukkitRunnable
{
	private BlockVector mMinBlock;
	private BlockVector mMaxBlock;
	private Island mIsland;
	private long mMaxIterationTime;
	
	private Iterator<Environment> mEnvIterator;
	private Environment mEnvCurrent;
	private int mX;
	private int mY;
	private int mZ;
	
	private int mScore;
	
	private PointLookup mLookup;
	private SkyblockWorld mWorld;
	
	private int[] mCounts = new int[Material.values().length];
	
	private boolean mIsRunning;
	
	public IslandScoreUpdater(Island island, int timeLimit)
	{
		mIsland = island;
		
		mMinBlock = new BlockVector(island.getChunkCoord().getX() * 16 + island.getWorld().getManager().getIslandNeutralSize(), 0, island.getChunkCoord().getZ() * 16 + island.getWorld().getManager().getIslandNeutralSize());
		mMaxBlock = new BlockVector((island.getChunkCoord().getX() + island.getWorld().getIslandChunkSize()) * 16 - island.getWorld().getManager().getIslandNeutralSize() - 1, 255, (island.getChunkCoord().getZ() + island.getWorld().getIslandChunkSize()) * 16 - island.getWorld().getManager().getIslandNeutralSize() - 1);
		
		mMaxIterationTime = TimeUnit.NANOSECONDS.convert(timeLimit, TimeUnit.MILLISECONDS);
		
		mX = mMinBlock.getBlockX() - 1;
		mY = mMinBlock.getBlockY();
		mZ = mMinBlock.getBlockZ();
		
		mEnvIterator = Iterators.forArray(Environment.values());
		mEnvCurrent = mEnvIterator.next();
		
		mLookup = mIsland.getWorld().getManager().getPointLookup();
		mWorld = mIsland.getWorld();
		
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
			
			World world = null;
			
			if (mEnvCurrent != null)
				world = mWorld.getWorld(mEnvCurrent);
			
			if (world == null)
			{
				if (mEnvIterator.hasNext())
				{
					mEnvCurrent = mEnvIterator.next();
					mX = mMinBlock.getBlockX() - 1;
					mY = mMinBlock.getBlockY();
					mZ = mMinBlock.getBlockZ();
					continue;
				}
				else
					// Iteration is done
					break;
			}
		
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
					{
						mEnvCurrent = null;
						continue; // Do next world iteration
					}
				}
			}
			
			// Calculate score
			Block block = world.getBlockAt(mX, mY, mZ);
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
		
		// Check challenges based on the island
		for (Challenge challenge : mIsland.getWorld().getManager().getChallenges().getChallenges())
		{
			if (challenge instanceof IslandBasedChallenge)
				((IslandBasedChallenge)challenge).onCalculateScore(mIsland, mCounts);
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
