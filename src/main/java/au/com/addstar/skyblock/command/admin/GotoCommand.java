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

public class GotoCommand implements ICommand
{
	private SkyblockManager mManager;
	public GotoCommand(SkyblockManager manager)
	{
		mManager = manager;
	}
	
	@Override
	public String getName()
	{
		return "goto";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {"tp"};
	}

	@Override
	public String getPermission()
	{
		return "skyblock.commands.goto";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <island>";
	}

	@Override
	public String getDescription()
	{
		return "Teleports you to the specified island";
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

		OfflinePlayer owner = Utilities.getPlayer(args[0]);
		if (owner == null)
			throw new BadArgumentException(0, "Unknown player");
		
		Island island = mManager.getIsland(owner.getUniqueId());
		
		if (island == null)
			throw new BadArgumentException(0, "That player doesnt have an island");
		
		Player player = (Player)sender;
		player.teleport(island.getIslandSpawn());
		
		player.sendMessage(Utilities.format("&6[Skyblock] &fYou have been teleported to %s's island", island.getOwnerName()));
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}

}
