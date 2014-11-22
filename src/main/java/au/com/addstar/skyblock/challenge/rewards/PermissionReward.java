package au.com.addstar.skyblock.challenge.rewards;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import au.com.addstar.skyblock.SkyblockPlugin;
import au.com.addstar.skyblock.vault.IVault;

public class PermissionReward extends Reward
{
	private String mPerm;
	private String mName;
	
	public PermissionReward(String name, String perm)
	{
		mName = name;
		mPerm = perm;
	}
	
	@Override
	public void apply( Player player )
	{
		IVault vault = JavaPlugin.getPlugin(SkyblockPlugin.class).getManager().getVault();
		vault.addPermission(player, mPerm);
	}

	@Override
	public String getName()
	{
		return mName;
	}
}
