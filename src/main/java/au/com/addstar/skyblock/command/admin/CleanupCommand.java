package au.com.addstar.skyblock.command.admin;

import java.util.EnumSet;
import java.util.List;

import org.apache.commons.lang.time.DateFormatUtils;
import org.bukkit.command.CommandSender;

import au.com.addstar.monolith.command.BadArgumentException;
import au.com.addstar.monolith.command.CommandSenderType;
import au.com.addstar.monolith.command.ICommand;
import au.com.addstar.skyblock.SkyblockManager;
import au.com.addstar.skyblock.misc.Callback;
import au.com.addstar.skyblock.misc.ConfirmationPrompt;
import au.com.addstar.skyblock.misc.Utilities;
import au.com.addstar.skyblock.misc.ValueCallback;

public class CleanupCommand implements ICommand
{
	private SkyblockManager mManager;
	public CleanupCommand(SkyblockManager manager)
	{
		mManager = manager;
	}
	
	@Override
	public String getName()
	{
		return "cleanup";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {"purge"};
	}

	@Override
	public String getPermission()
	{
		return "skyblock.commands.cleanup";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " [<cutoff>]";
	}

	@Override
	public String getDescription()
	{
		return "Removes all islands older than the specified cutoff point, or the date defined in the config";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.allOf(CommandSenderType.class);
	}

	@Override
	public boolean onCommand( final CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		if (args.length > 1)
			return false;
		
		long date;
		if (args.length == 1)
		{
			try
			{
				date = Utilities.parseTimeDiff(args[0]);
			}
			catch(IllegalArgumentException e)
			{
				throw new BadArgumentException(0, "Expected date diff. eg. 4mo, 1y, 1mo2w, etc.");
			}
		}
		else
			date = mManager.getCleanupCutoff();
		
		final long fDate = System.currentTimeMillis() - date;
		
		new ConfirmationPrompt()
			.setPlayer(sender)
			.setText(Utilities.format("&6[Skyblock] &fAbandon all islands older than %s? This cannot be revered.", DateFormatUtils.ISO_DATE_TIME_ZONE_FORMAT.format(fDate)))
			.setCallback(new Callback()
			{
				@Override
				public void onComplete( boolean success, Throwable exception )
				{
					if (success)
					{
						sender.sendMessage(Utilities.format("&6[Skyblock] &eIsland cleanup is running"));
						ValueCallback<Integer> callback = new ValueCallback<Integer>()
						{
							@Override
							public void onComplete( boolean success, Integer value, Throwable exception )
							{
								sender.sendMessage(Utilities.format("&6[Skyblock] &fIsland cleanup complete. Abandoned %d islands", value));
							}
						};
						
						mManager.runCleanup(fDate, callback);
					}
				}
			})
			.launch();
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}

}
