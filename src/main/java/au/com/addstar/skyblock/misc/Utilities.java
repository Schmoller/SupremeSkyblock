package au.com.addstar.skyblock.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import au.com.addstar.monolith.lookup.Lookup;
import au.com.addstar.monolith.lookup.MaterialDefinition;
import au.com.addstar.skyblock.SkyblockPlugin;
import au.com.addstar.skyblock.island.Island;

public class Utilities
{
	public static final UUID nobody = UUID.fromString("00000000-0000-0000-0000-000000000000");
	
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
			if (!result.hasPlayedBefore() && !result.isOnline())
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
	
	private static Pattern mDiffPattern;
	private static TimeUnit[] mUnits = new TimeUnit[] {TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES, TimeUnit.SECONDS};
	private static String[] mUnitNames = new String[] {"d", "h", "m", "s"};
	
	public static long parseTimeDiff(String diffString)
	{
		// Prepare the parsing expression
		if (mDiffPattern == null)
		{
			StringBuilder builder = new StringBuilder();
			for (String unit : mUnitNames)
			{
				builder.append("(?:([0-9]+)[");
				builder.append(unit);
				builder.append(unit.toUpperCase());
				builder.append("])*");
			}
			
			mDiffPattern = Pattern.compile(builder.toString());
		}
		
		long time = 0;
		
		diffString = diffString.replace(" ", "");
		Matcher match = mDiffPattern.matcher(diffString);
		
		if (!match.matches())
			throw new IllegalArgumentException("Unknown date diff format");
		
		for (int i = 0; i < mUnits.length; ++i)
		{
			if (match.group(i+1) != null)
			{
				TimeUnit unit = mUnits[i];
				time += unit.toMillis(Integer.parseInt(match.group(i+1)));
			}
		}
		
		return time;
	}
	
	public static long parseTimeDiffSafe(String diffString, long def, Logger logger)
	{
		try
		{
			return parseTimeDiff(diffString);
		}
		catch(IllegalArgumentException e)
		{
			logger.severe("Failed to parse date diff '" + diffString + "'. Reason: " + e.getMessage());
			return def;
		}
	}
	
	public static List<Player> getPlayersOnIsland(Island island)
	{
		Location temp = new Location(null, 0, 0, 0); 
		ArrayList<Player> players = new ArrayList<Player>();
		for (Player player : island.getWorld().getWorld().getPlayers())
		{
			if (island.getWorld().getIslandAt(player.getLocation(temp)) == island)
				players.add(player);
		}
		
		return players;
	}
	
	public static void sendPlayerHome(Player player)
	{
		Island theirIsland = SkyblockPlugin.getPlugin(SkyblockPlugin.class).getManager().getIsland(player.getUniqueId());
		if (theirIsland != null)
			player.teleport(theirIsland.getIslandSpawn());
		else
			player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
	}
	
	public static void updateNames(Player player, Island island)
	{
		String name = ChatColor.stripColor(player.getDisplayName());
		
		if (island.getOwner().equals(player.getUniqueId()))
		{
			if (!island.getOwnerName().equals(name))
				island.setOwnerName(name);
		}
		else if (island.getMembers().contains(player.getUniqueId()))
		{
			if (!island.getMemberName(player.getUniqueId()).equals(name))
				island.setMemberName(player.getUniqueId(), name);
		}
	}
}
