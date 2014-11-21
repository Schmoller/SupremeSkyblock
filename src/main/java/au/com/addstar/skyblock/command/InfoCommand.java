package au.com.addstar.skyblock.command;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.addstar.monolith.command.BadArgumentException;
import au.com.addstar.monolith.command.CommandSenderType;
import au.com.addstar.monolith.command.ICommand;
import au.com.addstar.skyblock.SkyblockManager;
import au.com.addstar.skyblock.island.Island;
import au.com.addstar.skyblock.misc.Utilities;

public class InfoCommand implements ICommand
{
	private SkyblockManager mManager;
	
	public InfoCommand(SkyblockManager manager)
	{
		mManager = manager;
	}
	
	@Override
	public String getName()
	{
		return "info";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "skyblock.commands.info";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		if (sender.hasPermission("skyblock.commands.info.others"))
			return label + " [<player>]";
		return label;
	}

	@Override
	public String getDescription()
	{
		return "Shows information about the island you are on";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player, CommandSenderType.Console);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		if (args.length > 1)
			return false;
		
		Island island;
		if (args.length == 1)
		{
			if (!sender.hasPermission("skyblock.commands.info.others"))
				return false;
			
			OfflinePlayer lookup = Utilities.getPlayer(args[0]);
			if (lookup == null)
				throw new BadArgumentException(0, "Unknown player");
			else
				island = mManager.getIsland(lookup.getUniqueId());
			
			if (island == null)
			{
				sender.sendMessage(ChatColor.GOLD + lookup.getName() + " does not have an island");
				return true;
			}
		}
		else if (!(sender instanceof Player))
			throw new IllegalArgumentException("A players name is required when called from the console");
		else
		{
			island = mManager.getIslandAt(((Player)sender).getLocation());
			if (island == null)
			{
				sender.sendMessage(ChatColor.GOLD + "This area is not owned by anyone.");
				return true;
			}
		}
		
		sender.sendMessage(ChatColor.GOLD + "[Skyblock]" + ChatColor.WHITE + " Island info:");
		sender.sendMessage(ChatColor.GRAY + " Owner:" + ChatColor.WHITE + " " + island.getOwnerName());
		sender.sendMessage(ChatColor.GRAY + " Location:" + ChatColor.WHITE + String.format(" %d,%d,%d %s", island.getIslandOrigin().getBlockX(), island.getIslandOrigin().getBlockY(), island.getIslandOrigin().getBlockZ(), island.getIslandOrigin().getWorld().getName()));
		int rank = island.getRank();
		if (rank < 0)
			sender.sendMessage(ChatColor.GRAY + " Rank:" + ChatColor.WHITE + " Unranked");
		else
			sender.sendMessage(ChatColor.GRAY + " Rank:" + ChatColor.WHITE + " " + island.getRank());
		sender.sendMessage(ChatColor.GRAY + " Score:" + ChatColor.WHITE + " " + island.getScore());
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}
}
