package au.com.addstar.skyblock.command;

import au.com.addstar.monolith.command.RootCommandDispatcher;
import au.com.addstar.skyblock.SkyblockManager;

public class BaseCommand extends RootCommandDispatcher
{
	public BaseCommand(SkyblockManager manager)
	{
		super("Allows you to use skyblock");
		
		// User commands
		setDefault(new DefaultCommand(manager));
		registerCommand(new RestartCommand(manager));
		registerCommand(new InfoCommand(manager));
		registerCommand(new ConfirmCommand());
		registerCommand(new CancelCommand());
		
		// Admin commands
		registerCommand(new TemplateCommand(manager));
	}
}
