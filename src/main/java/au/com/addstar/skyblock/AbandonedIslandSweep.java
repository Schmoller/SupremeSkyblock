package au.com.addstar.skyblock;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import au.com.addstar.skyblock.island.Island;
import au.com.addstar.skyblock.misc.Utilities;

public class AbandonedIslandSweep
{
	private Plugin mPlugin;
	private LinkedBlockingQueue<Island> mWaitingIslands;
	private long mDelayTime;
	
	public AbandonedIslandSweep(Plugin plugin)
	{
		mPlugin = plugin;
		mWaitingIslands = new LinkedBlockingQueue<Island>();
	}
	
	public void load(ConfigurationSection config)
	{
		if (config.isConfigurationSection("general"))
		{
			ConfigurationSection section = config.getConfigurationSection("general");
			mDelayTime = Utilities.parseTimeDiffSafe(section.getString("abandon-sweep-delay", "2m"), TimeUnit.MINUTES.toMillis(2), mPlugin.getLogger());
			mDelayTime /= 50;
		}
		else
			mDelayTime = 2400;
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
			new BukkitRunnable()
			{
				@Override
				public void run()
				{
					if (!mWaitingIslands.isEmpty())
					{
						Island island = mWaitingIslands.poll();
						island.getWorld().removeIsland(island);
					}
					else
					{
						cancel();
						startDelayTask();
					}
				}
			}.runTaskTimer(mPlugin, 4, 4);
		}
		else
			startDelayTask();
	}
}
