package au.com.addstar.skyblock.command;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.addstar.monolith.command.BadArgumentException;
import au.com.addstar.monolith.command.CommandSenderType;
import au.com.addstar.monolith.command.ICommand;
import au.com.addstar.skyblock.SkyblockManager;
import au.com.addstar.skyblock.island.Island;
import au.com.addstar.skyblock.misc.Utilities;

public class HomeCommand implements ICommand
{
	private SkyblockManager mManager;
	
	public HomeCommand(SkyblockManager manager)
	{
		mManager = manager;
	}
	
	@Override
	public String getName()
	{
		return "home";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {"h"};
	}

	@Override
	public String getPermission()
	{
		return "skyblock.command.home";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " [<island>]";
	}

	@Override
	public String getDescription()
	{
		return "Teleports to your island, or one of the islands you are a member of";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		if (args.length > 1)
			return false;
		
		Player player = (Player)sender;
		
		Island island;
		if (args.length == 1)
		{
			OfflinePlayer other = Utilities.getPlayer(args[0]);
			if (other == null)
				throw new BadArgumentException(0, "Unknown player");
			
			island = mManager.getIsland(other.getUniqueId());
			
			if (island == null)
				throw new IllegalArgumentException("That player doesnt have an island");
		}
		else
		{
			island = mManager.getIsland(player.getUniqueId());
			if (island == null)
				throw new IllegalArgumentException("You don't have an island. Use '" + parent + "' to create one");
		}
		
		if (!island.canAssist(player))
			throw new IllegalArgumentException("You do not own, and are not a member of that island");
		
		player.teleport(island.getIslandSpawn());
		Utilities.updateNames(player, island);
		if (island.getOwner().equals(player.getUniqueId()))
			player.sendMessage(Utilities.format("&6[Skyblock] &aYou have been teleported to your island"));
		else
			player.sendMessage(Utilities.format("&6[Skyblock] &aYou have been teleported to %s's island", island.getOwnerName()));
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}
}
