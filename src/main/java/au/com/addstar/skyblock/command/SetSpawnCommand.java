package au.com.addstar.skyblock.command;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.addstar.monolith.command.BadArgumentException;
import au.com.addstar.monolith.command.CommandSenderType;
import au.com.addstar.monolith.command.ICommand;
import au.com.addstar.skyblock.SkyblockManager;
import au.com.addstar.skyblock.island.Island;
import au.com.addstar.skyblock.misc.Utilities;

public class SetSpawnCommand implements ICommand
{
	private SkyblockManager mManager;
	
	public SetSpawnCommand(SkyblockManager manager)
	{
		mManager = manager;
	}
	
	@Override
	public String getName()
	{
		return "setspawn";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "skyblock.command.setspawn";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label;
	}

	@Override
	public String getDescription()
	{
		return "Sets the spawn location of your island";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player);
	}

	@SuppressWarnings( "deprecation" )
	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		if (args.length != 0)
			return false;
		
		Player player = (Player)sender;
		
		Island island = mManager.getIslandAt(player.getLocation());
		
		if (island == null)
			throw new IllegalArgumentException("You are not standing on an island");
		
		if (!island.getOwner().equals(player))
			throw new IllegalArgumentException("You are not the owner of this island");
		
		if (!player.isOnGround())
			throw new IllegalArgumentException("You are not standing on the ground");
		
		island.setIslandSpawn(player.getLocation());
		island.save();
		
		player.sendMessage(Utilities.format("&6[Skyblock] &aYou have set the islands spawn point.", parent));
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}
}
