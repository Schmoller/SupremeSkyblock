package au.com.addstar.skyblock.command.admin;

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

public class AbandonCommand implements ICommand
{
	private SkyblockManager mManager;
	
	public AbandonCommand(SkyblockManager manager)
	{
		mManager = manager;
	}
	
	@Override
	public String getName()
	{
		return "abandon";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "skyblock.command.abandon";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " [<island>]";
	}

	@Override
	public String getDescription()
	{
		return "Abandons an island which will then be removed on the next removal sweep";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.allOf(CommandSenderType.class);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		if (args.length > 1)
			return false;
		
		Island island;
		
		if (args.length == 1)
		{
			OfflinePlayer player = Utilities.getPlayer(args[0]);
			if (player == null)
				throw new BadArgumentException(0, "Unknown player");
			
			island = mManager.getIsland(player.getUniqueId());
			if (island == null)
				throw new BadArgumentException(0, "That player does not have an island");
		}
		else if (sender instanceof Player)
		{
			island = mManager.getIslandAt(((Player)sender).getLocation(), false);
			if (island == null)
				throw new IllegalArgumentException("There is no island where you are standing");
		}
		else
			throw new IllegalArgumentException("You must specify an island when running from the console");
		
		island.abandonIsland();
		sender.sendMessage(Utilities.format("&6%s's island has been abandoned", island.getOwnerName()));
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}

}
