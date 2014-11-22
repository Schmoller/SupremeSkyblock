package au.com.addstar.skyblock.challenge.rewards;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import au.com.addstar.skyblock.SkyblockPlugin;
import au.com.addstar.skyblock.vault.IVault;

public class PermissionGroupReward extends Reward
{
	private String mGroup;
	private String mName;
	
	public PermissionGroupReward(String name, String group)
	{
		mName = name;
		mGroup = group;
	}
	
	@Override
	public void apply( Player player )
	{
		IVault vault = JavaPlugin.getPlugin(SkyblockPlugin.class).getManager().getVault();
		vault.addGroup(player, mGroup);
	}

	@Override
	public String getName()
	{
		return mName;
	}
}
