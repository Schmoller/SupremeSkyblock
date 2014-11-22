package au.com.addstar.skyblock.challenge.rewards;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import au.com.addstar.skyblock.SkyblockPlugin;
import au.com.addstar.skyblock.vault.IVault;

public class MoneyReward extends Reward
{
	private double mAmount;
	
	public MoneyReward(double amount)
	{
		mAmount = amount;
	}
	
	@Override
	public void apply( Player player )
	{
		IVault vault = JavaPlugin.getPlugin(SkyblockPlugin.class).getManager().getVault();
		vault.depositMoney(player, mAmount);
	}

	@Override
	public String getName()
	{
		IVault vault = JavaPlugin.getPlugin(SkyblockPlugin.class).getManager().getVault();
		return vault.formatMoney(mAmount);
	}

}
