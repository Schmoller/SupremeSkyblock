package au.com.addstar.skyblock.command;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

public class InviteCommand implements ICommand
{
	private SkyblockManager mManager;
	
	public InviteCommand(SkyblockManager manager)
	{
		mManager = manager;
	}
	
	@Override
	public String getName()
	{
		return "invite";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "skyblock.command.invite";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <player>";
	}

	@Override
	public String getDescription()
	{
		return "Invites a player to join your skyblock";
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
		final Island island = mManager.getIsland(player.getUniqueId());
		if (island == null)
			throw new IllegalArgumentException("You do not have an island. Please create one with " + parent + " before inviting players");
		
		if (mManager.getIslandMaxMembers() != -1 && island.getMembers().size() >= mManager.getIslandMaxMembers())
			throw new IllegalArgumentException("You may only have " + mManager.getIslandMaxMembers() + " players registered to your island at once. Please remove a player with " + parent + " kick <player> before inviting anymore players");
		
		final Player invitee = Bukkit.getPlayer(args[0]);
		if (invitee == null)
			throw new BadArgumentException(0, "Unknown player");
		
		int theirCount = mManager.getIslands(invitee.getUniqueId()).size();
		if (mManager.getPlayerExcludeOwn() && mManager.getIsland(invitee.getUniqueId()) != null)
			--theirCount;
			
		if (theirCount >= mManager.getPlayerMaxMembership())
			throw new IllegalArgumentException(ChatColor.stripColor(invitee.getDisplayName()) + " can not be part of any more islands. They will have to leave one to be able to join yours.");
		
		Callback callback = new Callback()
		{
			@Override
			public void onComplete( boolean success, Throwable exception )
			{
				if (success)
				{
					// Redo checks to make sure that conditions have not changed since the request was made
					if (mManager.getIslandMaxMembers() != -1 && island.getMembers().size() >= mManager.getIslandMaxMembers())
					{
						player.sendMessage(Utilities.format("&6[Skyblock] &c%s was unable to accept your request, you have reached the limit for members on your island.", invitee.getDisplayName()));
						invitee.sendMessage(Utilities.format("&6[Skyblock] &cUnable to accept the invite, %s's island now has too many members", player.getDisplayName()));
						return;
					}
					
					int theirCount = mManager.getIslands(invitee.getUniqueId()).size();
					if (mManager.getPlayerExcludeOwn() && mManager.getIsland(invitee.getUniqueId()) != null)
						--theirCount;
						
					if (theirCount >= mManager.getPlayerMaxMembership())
					{
						player.sendMessage(Utilities.format("&6[Skyblock] &c%s was unable to accept your request, they are a member of too many islands", invitee.getDisplayName()));
						invitee.sendMessage(Utilities.format("&6[Skyblock] &cYou are a member of too many islands, you will have to leave one to join %s' island.", player.getDisplayName()));
						return;
					}
					
					// All is well
					island.addMember(invitee);
					player.sendMessage(Utilities.format("&6[Skyblock] &f%s has joined your island", invitee.getDisplayName()));
					invitee.sendMessage(Utilities.format("&6[Skyblock] &fYou have joined %s's island. Use &a/is home %s&f to get there", player.getDisplayName(), player.getDisplayName()));
				}
				else
					player.sendMessage(Utilities.format("&6[Skyblock] &e%s rejected your invite", invitee.getDisplayName()));
			}
		};
		
		new ConfirmationPrompt()
			.setPlayer(invitee)
			.setCallback(callback)
			.setText(Utilities.format("&6[Skyblock] &eYou have been invited to join %s's skyblock island.", player.getDisplayName()))
			.launch();
		
		player.sendMessage(Utilities.format("&6[Skyblock] &fYou have invited %s to join your island.", invitee.getDisplayName()));
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}

}
