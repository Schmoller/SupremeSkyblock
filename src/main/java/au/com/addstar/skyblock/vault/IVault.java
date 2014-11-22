package au.com.addstar.skyblock.vault;

import org.bukkit.entity.Player;

public interface IVault
{
	public void depositMoney(Player player, double amount);
	
	public String formatMoney(double amount);
	
	public void addPermission(Player player, String perm);
	
	public void addGroup(Player player, String group);
}
