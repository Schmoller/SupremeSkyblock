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

public class KickCommand implements ICommand
{
	private SkyblockManager mManager;
	
	public KickCommand(SkyblockManager manager)
	{
		mManager = manager;
	}
	
	@Override
	public String getName()
	{
		return "kick";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "skyblock.command.kick";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <player>";
	}

	@Override
	public String getDescription()
	{
		return "Removes a player from your islands membership, and moves them off the island.";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		if (args.length != 1)
			return false;
		
		final Player player = (Player)sender;
		
		Island island = mManager.getIsland(player.getUniqueId());
		
		if (island == null)
			throw new IllegalArgumentException("You do not have an island to kick people from");
		
		OfflinePlayer other = Utilities.getPlayer(args[0]);
		if (other == null)
			throw new BadArgumentException(0, "Unknown player");
		
		boolean handled = false;
		if (island.removeMember(other))
		{
			player.sendMessage(Utilities.format("&6[Skyblock] &f%s has been kicked from your island", other.getName()));
			if (other.isOnline())
				other.getPlayer().sendMessage(Utilities.format("&6[Skyblock] &cYou are no longer a member of %s's island", player.getDisplayName()));
			
			handled = true;
		}
		
		// Regardless of prior membership, if they are on the island, move them off it
		if (other.isOnline())
		{
			Player otherPlayer = other.getPlayer();
			if (mManager.getIslandAt(otherPlayer.getLocation()) == island)
			{
				// They are on this island
				Utilities.sendPlayerHome(otherPlayer);
				
				otherPlayer.sendMessage(Utilities.format("&6[Skyblock] &cYou have been moved off %s's island", player.getDisplayName()));
				player.sendMessage(Utilities.format("&6[Skyblock] &c%s has been moved off your island", otherPlayer.getDisplayName()));
				handled = true;
			}
		}
		
		// When they were not a member, and they were not on the island
		if (!handled)
			player.sendMessage(Utilities.format("&6[Skyblock] &f%s was neither a member of your island, nor on your island. Nothing was done", other.getName()));
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}

}
