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
import au.com.addstar.skyblock.island.IslandTemplate;

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
			Location spawn = existing.getIslandSpawn();
			
			// In the future this should not be needed, but this is not saved yet
			if (spawn == null)
				spawn = mManager.getTemplate().getSpawnLocation(existing.getIslandOrigin());
			
			player.teleport(spawn);
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6[Skyblock] &fYou have been teleported to your skyblock island"));
		}
		// Create a new island
		else
		{
			SkyblockWorld world = mManager.getNextSkyblockWorld();
			Island island = world.createIsland(player);
			IslandTemplate template = mManager.getTemplate();
			
			Location loc = island.getIslandOrigin();
			island.setIslandSpawn(template.getSpawnLocation(loc));
			template.placeAt(loc);
			player.teleport(island.getIslandSpawn());
			
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
