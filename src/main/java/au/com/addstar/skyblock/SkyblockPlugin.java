package au.com.addstar.skyblock;

import org.bukkit.Bukkit;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import au.com.addstar.skyblock.command.BaseCommand;

public class SkyblockPlugin extends JavaPlugin
{
	private SkyblockManager mManager;
	
	@Override
	public void onEnable()
	{
		saveDefaultConfig();
		
		mManager = new SkyblockManager(this);
		new BaseCommand(mManager).registerAs(getCommand("skyblock"));
		
		Bukkit.getScheduler().runTask(this, new Runnable()
		{
			@Override
			public void run()
			{
				onPostEnable();
			}
		});
	}
	
	private void onPostEnable()
	{
		mManager.load();
		Bukkit.getPluginManager().registerEvents(new ProtectionListener(mManager), this);
		Bukkit.getPluginManager().registerEvents(new GameplayListener(mManager), this);
		
		mManager.init();
	}
	
	@Override
	public void onDisable()
	{
		mManager.save();
	}
	
	@Override
	public ChunkGenerator getDefaultWorldGenerator( String worldName, String id )
	{
		return new EmptyGenerator();
	}
	
	public SkyblockManager getManager()
	{
		return mManager;
	}
}
