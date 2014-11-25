package au.com.addstar.skyblock.command.admin;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.command.CommandSender;

import au.com.addstar.monolith.command.BadArgumentException;
import au.com.addstar.monolith.command.CommandDispatcher;
import au.com.addstar.monolith.command.CommandSenderType;
import au.com.addstar.monolith.command.ICommand;
import au.com.addstar.skyblock.SkyblockManager;

public class AdminBaseCommand extends CommandDispatcher implements ICommand
{
	public AdminBaseCommand(SkyblockManager manager)
	{
		super("Provides access to a whole bunch of admin commands");
		
		registerCommand(new AbandonCommand(manager));
		registerCommand(new TemplateCommand(manager));
		registerCommand(new GotoCommand(manager));
	}
	
	@Override
	public String getName()
	{
		return "admin";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "skyblock.command.admin";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <command>";
	}

	@Override
	public String getDescription()
	{
		return "Provides access to a whole bunch of admin commands";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.allOf(CommandSenderType.class);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		return dispatchCommand(sender, parent, label, args);
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return tabComplete(sender, parent, label, args);
	}
}
