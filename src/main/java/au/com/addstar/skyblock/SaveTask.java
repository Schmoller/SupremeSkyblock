package au.com.addstar.skyblock;

import org.bukkit.scheduler.BukkitRunnable;

public class SaveTask extends BukkitRunnable
{
	private SkyblockManager mManager;
	
	public SaveTask(SkyblockManager manager)
	{
		mManager = manager;
	}
	
	@Override
	public void run()
	{
		mManager.save();
	}
}
