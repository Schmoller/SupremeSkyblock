package au.com.addstar.skyblock.command;

import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.addstar.monolith.command.BadArgumentException;
import au.com.addstar.monolith.command.CommandSenderType;
import au.com.addstar.monolith.command.ICommand;
import au.com.addstar.skyblock.SkyblockManager;
import au.com.addstar.skyblock.island.Island;
import au.com.addstar.skyblock.misc.Utilities;

public class RankCommand implements ICommand
{
	private SkyblockManager mManager;
	
	public RankCommand(SkyblockManager manager)
	{
		mManager = manager;
	}
	
	@Override
	public String getName()
	{
		return "rank";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {"top"};
	}

	@Override
	public String getPermission()
	{
		return "skyblock.command.rank";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label;
	}

	@Override
	public String getDescription()
	{
		return "Shows your islands rank, and the top ranked islands";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.allOf(CommandSenderType.class);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		if (args.length != 0)
			return false;
		
		Island island = null;
		if (sender instanceof Player)
		{
			Player player = (Player)sender;
			island = mManager.getIslandAt(player.getLocation());
			if (island == null || !island.canAssist(player))
				island = mManager.getIsland(player.getUniqueId());
		}
		
		sender.sendMessage(Utilities.format("&6[Skyblock] &eRanked islands:"));
		
		if (island != null)
		{
			int rank = island.getRank();
			if (rank < 0)
				sender.sendMessage(Utilities.format("&7 Your island is unranked"));
			else
				sender.sendMessage(Utilities.format("&7 Your rank is &e%d", rank));
		}
		
		int rank = 1;
		for (Entry<Integer, Island> entry : mManager.getTopScores())
		{
			sender.sendMessage(Utilities.format("&e%d&7: &e%s", rank, entry.getValue().getOwnerName()));
			
			// Show only top 10 islands
			if (++rank >= 10)
				break;
		}
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}

}
