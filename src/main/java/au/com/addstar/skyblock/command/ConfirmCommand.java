package au.com.addstar.skyblock.command;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.addstar.monolith.command.CommandSenderType;
import au.com.addstar.monolith.command.ICommand;
import au.com.addstar.skyblock.misc.ConfirmationPrompt;

public class ConfirmCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "confirm";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {"accept", "yes"};
	}

	@Override
	public String getPermission()
	{
		return null;
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label;
	}

	@Override
	public String getDescription()
	{
		return "Confirms a prompt";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args )
	{
		if(args.length != 0)
			return false;
		
		ConfirmationPrompt.accept((Player)sender);
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent,
			String label, String[] args )
	{
		return null;
	}

}
