package au.com.addstar.skyblock.command;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.addstar.monolith.command.BadArgumentException;
import au.com.addstar.monolith.command.CommandSenderType;
import au.com.addstar.monolith.command.ICommand;
import au.com.addstar.skyblock.SkyblockManager;
import au.com.addstar.skyblock.island.Island;
import au.com.addstar.skyblock.misc.Utilities;

public class WarpCommand implements ICommand
{
	private SkyblockManager mManager;
	
	public WarpCommand(SkyblockManager manager)
	{
		mManager = manager;
	}
	
	@Override
	public String getName()
	{
		return "warp";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {"tp"};
	}

	@Override
	public String getPermission()
	{
		return "skyblock.command.warp";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <island>";
	}

	@Override
	public String getDescription()
	{
		return "Teleports to an island if it allows warps.";
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
		
		Player player = (Player)sender;
		
		OfflinePlayer other = Utilities.getPlayer(args[0]);
		if (other == null)
			throw new BadArgumentException(0, "Unknown player");
			
		Island island = mManager.getIsland(other.getUniqueId());

		if (island == null)
			throw new IllegalArgumentException("That player doesnt have an island");
		
		if (!island.getWarpAllowed())
			throw new IllegalArgumentException("That island has warps disabled");
		
		Location location = island.getIslandSpawn();
		
		Utilities.safeTeleport(player, location);
		Utilities.updateNames(player, island);
		player.sendMessage(Utilities.format("&6[Skyblock] &aYou have been teleported to %s's island", island.getOwnerName()));
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}
}
