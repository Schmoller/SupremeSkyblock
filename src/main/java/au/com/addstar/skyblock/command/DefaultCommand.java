package au.com.addstar.skyblock.command;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.addstar.monolith.command.BadArgumentException;
import au.com.addstar.monolith.command.CommandSenderType;
import au.com.addstar.monolith.command.ICommand;
import au.com.addstar.skyblock.SkyblockManager;
import au.com.addstar.skyblock.SkyblockWorld;
import au.com.addstar.skyblock.island.Island;
import au.com.addstar.skyblock.misc.Utilities;

public class DefaultCommand implements ICommand
{
	private SkyblockManager mManager;
	
	public DefaultCommand(SkyblockManager manager)
	{
		mManager = manager;
	}
	
	@Override
	public String getName()
	{
		return "";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "skyblock.commands.skyblock";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label;
	}

	@Override
	public String getDescription()
	{
		return "Takes you to your island, or creates a new island";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		Player player = (Player)sender;
		
		Island existing = mManager.getIsland(player.getUniqueId());
		// Go to the existing island
		if (existing != null)
		{
			Location location = existing.getSettings(player.getUniqueId()).getHome();
			if (location == null)
				location = existing.getIslandSpawn();
			
			Utilities.updateNames(player, existing);
			Utilities.safeTeleport(player, location);
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[Skyblock] &fYou have been teleported to your skyblock island"));
		}
		// Create a new island
		else
		{
			SkyblockWorld world = mManager.getNextSkyblockWorld();
			Island island = world.createIsland(player);
			Utilities.safeTeleport(player, island.getIslandSpawn());
			
			world.save();
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[Skyblock] &fA new island has been created for you."));
		}
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}
}
