package au.com.addstar.skyblock.command;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.addstar.monolith.command.BadArgumentException;
import au.com.addstar.monolith.command.CommandSenderType;
import au.com.addstar.monolith.command.ICommand;
import au.com.addstar.skyblock.SkyblockManager;
import au.com.addstar.skyblock.island.Island;
import au.com.addstar.skyblock.misc.Callback;
import au.com.addstar.skyblock.misc.ConfirmationPrompt;
import au.com.addstar.skyblock.misc.Utilities;

public class LeaveCommand implements ICommand
{
	private SkyblockManager mManager;
	
	public LeaveCommand(SkyblockManager manager)
	{
		mManager = manager;
	}
	
	@Override
	public String getName()
	{
		return "leave";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "skyblock.command.leave";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " [<island>]";
	}

	@Override
	public String getDescription()
	{
		return "Removes yourself from being a member of an island";
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
		
		final Player player = (Player)sender;
		
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
			island = mManager.getIslandAt(player.getLocation());
			
			if (island == null)
				throw new IllegalArgumentException("This area is un-assigned");
		}
		
		if (island.getOwner().equals(player.getUniqueId()))
			throw new IllegalArgumentException("You cannot leave your own island");
		
		if (!island.getMembers().contains(player.getUniqueId()))
			throw new IllegalArgumentException("You are not a member of that island");
		
		final Island fIsland = island;
		
		Callback callback = new Callback()
		{
			@Override
			public void onComplete( boolean success, Throwable exception )
			{
				if (success)
				{
					fIsland.removeMember(player);
					player.sendMessage(Utilities.format("&6[Skyblock] &eYou have left %s's island.", fIsland.getOwnerName()));
					Player owner = Bukkit.getPlayer(fIsland.getOwner());
					if (owner != null)
						owner.sendMessage(Utilities.format("&6[Skyblock] &e%s has left your island.", player.getDisplayName()));
				}
				else
					player.sendMessage(Utilities.format("&6[Skyblock] &fYou have decided not to leave"));
			}
		};
		
		new ConfirmationPrompt()
			.setPlayer(player)
			.setText(Utilities.format("&6[Skyblock] &fAre you sure you want to leave %s's island?", island.getOwnerName()))
			.setCallback(callback)
			.launch();
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}

}
