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

public class ToggleWarpCommand implements ICommand
{
	private SkyblockManager mManager;
	
	public ToggleWarpCommand(SkyblockManager manager)
	{
		mManager = manager;
	}
	
	@Override
	public String getName()
	{
		return "togglewarp";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "skyblock.command.togglewarp";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label;
	}

	@Override
	public String getDescription()
	{
		return "Toggles allowing or disallowing warping to your island";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		if (args.length != 0)
			return false;
		
		Player player = (Player)sender;
		
		Island island = mManager.getIsland(player.getUniqueId());

		if (island == null)
			throw new IllegalArgumentException("You do not have an island. Please create one first with /is");
		
		boolean allowed = !island.getWarpAllowed();
		island.setWarpAllowed(allowed);
		
		if (allowed)
			player.sendMessage(Utilities.format("&6[Skyblock] &aWarps are now allowed to your island"));
		else
			player.sendMessage(Utilities.format("&6[Skyblock] &aWarps are now blocked to your island"));
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}
}
