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
import au.com.addstar.skyblock.misc.Callback;
import au.com.addstar.skyblock.misc.ConfirmationPrompt;
import au.com.addstar.skyblock.misc.Utilities;

public class RestartCommand implements ICommand
{
	private SkyblockManager mManager;
	
	public RestartCommand(SkyblockManager manager)
	{
		mManager = manager;
	}
	
	@Override
	public String getName()
	{
		return "restart";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {"reset"};
	}

	@Override
	public String getPermission()
	{
		return "skyblock.commands.restart";
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
		return "Restarts your island";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player, CommandSenderType.Console);
	}

	@Override
	public boolean onCommand( final CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		if (args.length > 1)
			return false;
		
		Island island;
		boolean otherIsland;
		if (args.length == 1)
		{
			if (!sender.hasPermission("skyblock.commands.restart.others"))
				return false;
			
			OfflinePlayer lookup = Utilities.getPlayer(args[0]);
			if (lookup == null)
				throw new BadArgumentException(0, "Unknown player");
			else
				island = mManager.getIsland(lookup.getUniqueId());
			
			if (island == null)
				throw new IllegalArgumentException(lookup.getName() + " does not have an island");
		
			otherIsland = true;
			sender.sendMessage(ChatColor.GOLD + "[Skyblock] " + ChatColor.WHITE + lookup.getName() + "'s island has been reset");
		}
		else if (!(sender instanceof Player))
			throw new IllegalArgumentException("A players name is required when called from the console");
		else
		{
			island = mManager.getIsland(((Player)sender).getUniqueId());
			if (island == null)
				throw new IllegalArgumentException("You do not have an island");
			
			otherIsland = false;
		}
		
		final Island fIsland = island;
		final boolean fOtherIsland = otherIsland;
		
		Callback callback = new Callback()
		{
			@Override
			public void onComplete( boolean success, Throwable exception )
			{
				if (success)
				{
					fIsland.clear();
					fIsland.placeIsland();
					
					if (fOtherIsland)
						sender.sendMessage(ChatColor.GOLD + "[Skyblock] " + ChatColor.WHITE + fIsland.getOwnerName() + "'s island has been reset");
					else
						sender.sendMessage(ChatColor.GOLD + "[Skyblock] " + ChatColor.WHITE + "Your island has been reset");
				}
				else
					sender.sendMessage(ChatColor.GOLD + "[Skyblock] " + ChatColor.YELLOW + "Restart cancelled");
			}
		};
		
		if (sender instanceof Player)
		{
			new ConfirmationPrompt()
			.setPlayer(sender)
			.setText(ChatColor.translateAlternateColorCodes('&', "&6[Skyblock] &eAre you sure you wish to reset your island? This is irreversible."))
			.setCallback(callback)
			.launch();
		}
		else
			callback.onComplete(true, null);
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}

}
