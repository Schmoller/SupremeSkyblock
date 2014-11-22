package au.com.addstar.skyblock.vault;

import org.bukkit.entity.Player;

public class NullWrapper implements IVault
{
	@Override
	public void depositMoney( Player player, double amount )
	{
		// Do nothing
	}
	
	@Override
	public String formatMoney( double amount )
	{
		return String.format("$%.2f", amount);
	}

	@Override
	public void addPermission( Player player, String perm )
	{
		// Do nothing
	}

	@Override
	public void addGroup( Player player, String group )
	{
		// Do nothing
	}
}
