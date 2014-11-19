package au.com.addstar.skyblock.challenge.rewards;

import org.bukkit.entity.Player;

public abstract class Reward
{
	public abstract void apply(Player player);
	
	public abstract String getName();
}
