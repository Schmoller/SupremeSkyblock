package au.com.addstar.skyblock;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.bukkit.scheduler.BukkitRunnable;

import au.com.addstar.skyblock.island.Island;
import au.com.addstar.skyblock.misc.ValueCallback;

public class PurgeTask extends BukkitRunnable
{
	private Iterator<SkyblockWorld> mWorldIterator;
	private Iterator<Island> mIterator;
	private long mDateCutoff;
	private long mMaxIterationTime;
	
	private ValueCallback<Integer> mCallback;
	private int mCount;
	
	public PurgeTask(long minDate, Collection<SkyblockWorld> worlds, ValueCallback<Integer> callback)
	{
		mWorldIterator = worlds.iterator();
		mDateCutoff = minDate;
		mCallback = callback;
		
		mMaxIterationTime = TimeUnit.MILLISECONDS.toNanos(5);
		mCount = 0;
	}
	
	@Override
	public void run()
	{
		long start = System.nanoTime();
		
		if (mIterator == null)
		{
			if (mWorldIterator.hasNext())
			{
				SkyblockWorld world = mWorldIterator.next();
				mIterator = world.getGrid().getIslands().iterator();
			}
			else
			{
				if (mCallback != null)
					mCallback.onComplete(true, mCount, null);
				cancel();
				return;
			}
		}
		
		while (mIterator.hasNext())
		{
			if (System.nanoTime() - start > mMaxIterationTime)
				return;
			
			Island island = mIterator.next();
			
			if (island.getLastUseTime() < mDateCutoff)
			{
				island.abandonIsland();
				++mCount;
			}
		}
		
		mIterator = null;
	}

	public int getCount()
	{
		return mCount;
	}
}
