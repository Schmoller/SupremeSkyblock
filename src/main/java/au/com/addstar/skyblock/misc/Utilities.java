package au.com.addstar.skyblock.misc;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import au.com.addstar.monolith.lookup.Lookup;
import au.com.addstar.monolith.lookup.MaterialDefinition;

public class Utilities
{
	@SuppressWarnings( "deprecation" )
	public static OfflinePlayer getPlayer(String nameOrUUID)
	{
		try
		{
			UUID id = UUID.fromString(nameOrUUID);
			return Bukkit.getOfflinePlayer(id);
		}
		catch(IllegalArgumentException e)
		{
			OfflinePlayer result = Bukkit.getOfflinePlayer(nameOrUUID);
			if (!result.hasPlayedBefore())
				return null;
			return result;
		}
	}
	
	public static String format(String format, Object... args)
	{
		return ChatColor.translateAlternateColorCodes('&', String.format(format, args));
	}
	
	@SuppressWarnings( "deprecation" )
    public static MaterialDefinition getMaterial(String name)
	{
		// Bukkit name
		Material mat = Material.getMaterial(name.toUpperCase());
		if (mat != null)
			return new MaterialDefinition(mat, (short)-1);
		
		// Id
		try
		{
			short id = Short.parseShort(name);
			mat = Material.getMaterial(id);
		}
		catch(NumberFormatException e)
		{
		}
		
		if(mat != null)
			return new MaterialDefinition(mat, (short)-1);

		// ItemDB
		return Lookup.findItemByName(name);
	}
	
	public static ItemStack parseItem(String[] parts)
	{
		if (parts.length == 0)
			throw new IllegalArgumentException("Item must be in the format of: '<material> [<amount>]'");
		
		String dataStr = null;
		MaterialDefinition def;
		
		if (parts[0].contains(":"))
		{
			String name = parts[0].split(":")[0];
			dataStr = parts[0].split(":")[1];
			
			def = getMaterial(name);
		}
		else
			def = getMaterial(parts[0]);
		
		if (def == null)
			throw new IllegalArgumentException("Unknown material " + parts[0]);
		
		if (def.getData() < 0)
		{
			int data = 0;
			if (dataStr != null)
			{
				try
				{
					data = Integer.parseInt(dataStr);
					if (data < 0)
						throw new IllegalArgumentException("Data value cannot be less than 0");
				}
				catch(NumberFormatException e)
				{
					throw new IllegalArgumentException("Unable to parse data value " + dataStr);
				}
			}
			
			def = new MaterialDefinition(def.getMaterial(), (short)data);
		}
		
		if (parts.length >= 1)
		{
			int amount = -1;
			try
			{
				amount = Integer.parseInt(parts[1]);
				if (amount <= 0)
					throw new IllegalArgumentException("Amount cannot be less than 1");
			}
			catch(NumberFormatException e)
			{
				throw new IllegalArgumentException("Unable to parse amount " + parts[1]);
			}
			
			return def.asItemStack(amount);
		}
		else
		{
			ItemStack item = def.asItemStack(1);
			item.setAmount(item.getMaxStackSize());
			return item;
		}
	}
}
