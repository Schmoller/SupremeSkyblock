package au.com.addstar.skyblock.vault;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultWrapper implements IVault
{
	private Economy mEcon;
	private Permission mPerm;
	
	public VaultWrapper()
	{
		RegisteredServiceProvider<Economy> econProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
		RegisteredServiceProvider<Permission> permProvider = Bukkit.getServicesManager().getRegistration(Permission.class);
		
		mEcon = econProvider.getProvider();
		mPerm = permProvider.getProvider();
	}
	
	@Override
	public void depositMoney( Player player, double amount )
	{
		mEcon.depositPlayer(player, amount);
	}
	
	@Override
	public String formatMoney( double amount )
	{
		return mEcon.format(amount);
	}

	@Override
	public void addPermission( Player player, String perm )
	{
		mPerm.playerAdd(player, perm);
	}
	
	@Override
	public void addGroup( Player player, String group )
	{
		mPerm.playerAddGroup(player, group);
	}
}
