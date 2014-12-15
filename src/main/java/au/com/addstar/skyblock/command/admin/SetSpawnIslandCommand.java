package au.com.addstar.skyblock.command.admin;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.addstar.monolith.command.BadArgumentException;
import au.com.addstar.monolith.command.CommandSenderType;
import au.com.addstar.monolith.command.ICommand;
import au.com.addstar.skyblock.SkyblockManager;
import au.com.addstar.skyblock.SkyblockWorld;
import au.com.addstar.skyblock.island.Coord;
import au.com.addstar.skyblock.island.Island;
import au.com.addstar.skyblock.misc.Callback;
import au.com.addstar.skyblock.misc.ConfirmationPrompt;
import au.com.addstar.skyblock.misc.Utilities;

public class SetSpawnIslandCommand implements ICommand
{
	private SkyblockManager mManager;
	
	public SetSpawnIslandCommand(SkyblockManager manager)
	{
		mManager = manager;
	}
	
	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player);
	}

	@Override
	public String getDescription()
	{
		return "Sets the spawn island location";
	}

	@Override
	public String getName()
	{
		return "setspawnisland";
	}

	@Override
	public String getPermission()
	{
		return "skyblock.commands.setspawnisland";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label;
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		if (args.length != 0)
			return false;
		
		final Player player = (Player)sender;
		
		final SkyblockWorld world = mManager.getSkyblockWorld(player.getWorld()); 
		if (world == null)
			throw new IllegalArgumentException("You are not in a skyblock world");
		
		final Coord pos = world.getIslandCoordAt(player.getLocation());
		
		Island existing = world.getGrid().get(pos); 
		if (existing != null)
			throw new IllegalArgumentException("You cannot set the spawn island to be here. This space is occupied by " + existing.getOwnerName() + "'s island");
		
		Callback callback = new Callback()
		{
			@Override
			public void onComplete( boolean success, Throwable exception )
			{
				if (success)
				{
					world.setSpawnIsland(world.getIslandCoordAt(player.getLocation()));
					world.save();
					player.sendMessage(Utilities.format("&6[Skyblock] &fSpawn island has been set to your location"));
				}
				else
					player.sendMessage(Utilities.format("&6[Skyblock] &eSpawn island will not be changed"));
			}
		};
		
		if (world.getSpawnIsland() != null)
		{
			new ConfirmationPrompt()
				.setText(Utilities.format("&6[Skyblock] &fThe spawn island already exists. If you continue, you will be deleting the existing spawn island. Do you wish to continue?"))
				.setCallback(callback)
				.setPlayer(player)
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
