package au.com.addstar.skyblock.challenge.rewards;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import au.com.addstar.skyblock.misc.Utilities;

public abstract class Reward
{
	public abstract void apply(Player player);
	
	public abstract String getName();
	
	public static Reward load(String definition)
	{
		String[] parts = definition.trim().split(" ");
		
		if (parts[0].equalsIgnoreCase("item"))
		{
			if (parts.length > 3)
				throw new IllegalArgumentException("Item rewards should be in the format: 'item <material>' or 'item <material> <amount>'");
			
			ItemStack item = Utilities.parseItem(Arrays.copyOfRange(parts, 1, parts.length));
			return new ItemReward(item);
		}
		else if (parts[0].equalsIgnoreCase("xp"))
		{
			if (parts.length != 2)
				throw new IllegalArgumentException("XP rewards should be in the format: 'xp <amount>' or 'xp <amount>L' for levels");
			
			boolean levels = false;
			if (parts[1].endsWith("L"))
			{
				levels = true;
				parts[1] = parts[1].substring(0, parts[1].length()-1);
			}
			
			try
			{
				int amount = Integer.parseInt(parts[1]);
				if (amount < 0)
					throw new IllegalArgumentException("XP rewards should be in the format: 'xp <amount>' or 'xp <amount>L' for levels");
				
				return new XPReward(amount, levels);
			}
			catch(NumberFormatException e)
			{
				throw new IllegalArgumentException("XP rewards should be in the format: 'xp <amount>' or 'xp <amount>L' for levels");
			}
		}
		else
			throw new IllegalArgumentException("Unknown reward type " + parts[0]);
	}
}
