package au.com.addstar.skyblock;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import au.com.addstar.skyblock.island.Island;
import au.com.addstar.skyblock.island.IslandScoreUpdater;
import au.com.addstar.skyblock.misc.Utilities;

public class ScoreUpdateSweep
{
	private Plugin mPlugin;
	private LinkedBlockingQueue<Island> mWaitingIslands;
	private long mDelayTime;
	private int mProcessTimeLimit;
	
	private IslandScoreUpdater mUpdater;
	
	public ScoreUpdateSweep(Plugin plugin)
	{
		mPlugin = plugin;
		mWaitingIslands = new LinkedBlockingQueue<Island>();
	}
	
	public void load(ConfigurationSection config)
	{
		if (config.isConfigurationSection("general"))
		{
			ConfigurationSection section = config.getConfigurationSection("general");
			mDelayTime = Utilities.parseTimeDiffSafe(section.getString("score-sweep-delay", "2m"), TimeUnit.MINUTES.toMillis(2), mPlugin.getLogger());
			mDelayTime /= 50;
			
			mProcessTimeLimit = section.getInt("score-calc-time-limit", 10);
			if (mProcessTimeLimit <= 0)
				mProcessTimeLimit = 10;
		}
		else
		{
			mDelayTime = 2400;
			mProcessTimeLimit = 10;
		}
	}
	
	public void queueIsland(Island island)
	{
		mWaitingIslands.add(island);
	}
	
	public void start()
	{
		startDelayTask();
	}
	
	private void startDelayTask()
	{
		Bukkit.getScheduler().runTaskLater(mPlugin, new Runnable()
		{
			@Override
			public void run()
			{
				processQueue();
			}
		}, mDelayTime);
	}
	
	private void processQueue()
	{
		if (!mWaitingIslands.isEmpty())
		{
			Island island = mWaitingIslands.poll();
			// Calculate task
			mUpdater = new IslandScoreUpdater(island, mProcessTimeLimit);
			mUpdater.runTaskTimer(mPlugin, 1, 1);
			
			// Wait task
			new BukkitRunnable()
			{
				@Override
				public void run()
				{
					if (!mUpdater.isRunning())
					{
						cancel();
						processQueue();
					}
				}
			}.runTaskTimer(mPlugin, 1, 1);
		}
		else
			startDelayTask();
	}
}
