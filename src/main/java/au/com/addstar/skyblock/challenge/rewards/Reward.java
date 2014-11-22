package au.com.addstar.skyblock.challenge.rewards;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
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
		else if (parts[0].equalsIgnoreCase("cmd"))
		{
			if (parts.length == 1)
				throw new IllegalArgumentException("Command rewards should be in the format: 'cmd <name>;<command>' or 'cmd <command>'");
			
			String full = ChatColor.translateAlternateColorCodes('&', StringUtils.join(parts, " ", 1, parts.length));
			String name;
			String command;
			if (full.contains(";"))
			{
				String[] split = full.split(";", 2);
				name = split[0];
				command = split[1];
			}
			else
				name = command = full;
			
			return new CommandReward(command, name);
		}
		else if (parts[0].equalsIgnoreCase("perm"))
		{
			if (parts.length == 1)
				throw new IllegalArgumentException("Permission rewards should be in the format: 'perm <name>;<perm>' or 'perm <perm>'");
			
			String full = ChatColor.translateAlternateColorCodes('&', StringUtils.join(parts, " ", 1, parts.length));
			String name;
			String perm;
			if (full.contains(";"))
			{
				String[] split = full.split(";", 2);
				name = split[0];
				perm = split[1];
			}
			else
				name = perm = full;
			
			return new PermissionReward(name, perm);
		}
		else if (parts[0].equalsIgnoreCase("group"))
		{
			if (parts.length == 1)
				throw new IllegalArgumentException("Permission group rewards should be in the format: 'group <name>;<group>' or 'group <group>'");
			
			String full = ChatColor.translateAlternateColorCodes('&', StringUtils.join(parts, " ", 1, parts.length));
			String name;
			String group;
			if (full.contains(";"))
			{
				String[] split = full.split(";", 2);
				name = split[0];
				group = split[1];
			}
			else
				name = group = full;
			
			return new PermissionGroupReward(name, group);
		}
		else if (parts[0].equalsIgnoreCase("money"))
		{
			if (parts.length != 2)
				throw new IllegalArgumentException("Money rewards should be in the format: 'money <amount>'");
			
			String raw = parts[1];
			// Attempt to remove any currency symbols
			int start = 0;
			int end = raw.length();
			
			while(start < end && !Character.isDigit(raw.charAt(start)))
				++start;
			
			while(end > start & !Character.isDigit(raw.charAt(end-1)))
				--end;
			
			if (start >= end)
				throw new IllegalArgumentException("Money rewards should be in the format: 'money <amount>'");
			
			try
			{
				double amount = Double.parseDouble(raw.substring(start, end));
				if (amount < 0)
					throw new IllegalArgumentException("Money rewards should be in the format: 'money <amount>'. No negative values");
				
				return new MoneyReward(amount);
			}
			catch(NumberFormatException e)
			{
				throw new IllegalArgumentException("Money rewards should be in the format: 'money <amount>'");
			}
		}
		else
			throw new IllegalArgumentException("Unknown reward type " + parts[0]);
	}
}
