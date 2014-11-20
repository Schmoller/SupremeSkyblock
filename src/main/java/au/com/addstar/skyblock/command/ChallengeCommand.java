package au.com.addstar.skyblock.command;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import au.com.addstar.monolith.Monolith;
import au.com.addstar.monolith.command.BadArgumentException;
import au.com.addstar.monolith.command.CommandSenderType;
import au.com.addstar.monolith.command.ICommand;
import au.com.addstar.skyblock.SkyblockManager;
import au.com.addstar.skyblock.challenge.Challenge;
import au.com.addstar.skyblock.island.Island;
import au.com.addstar.skyblock.misc.Utilities;

public class ChallengeCommand implements ICommand
{
	private SkyblockManager mManager;
	
	public ChallengeCommand(SkyblockManager manager)
	{
		mManager = manager;
	}
	
	@Override
	public String getName()
	{
		return "challenge";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {"c"};
	}

	@Override
	public String getPermission()
	{
		return "skyblock.command.challenge";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " [complete <name>|<name>]";
	}

	@Override
	public String getDescription()
	{
		return "Lists or completes challenges";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		if (args.length > 2)
			return false;
		
		Player player = (Player)sender;
		
		if (args.length != 0)
		{
			// Completing challenges
			if (args.length == 2)
			{
				if (!args[0].equalsIgnoreCase("complete") && !args[0].equalsIgnoreCase("c"))
					throw new BadArgumentException(0, "Expected complete, or c");
				
				Island island = mManager.getIslandAt(player.getLocation());
				
				if (island == null || !island.getOwner().equals(player.getUniqueId()))
					throw new IllegalArgumentException("You are not on your island, you may not complete challenges");
				
				try
				{
					Challenge challenge = mManager.getChallenges().getChallenge(args[1]);
					if (challenge == null)
						throw new BadArgumentException(1, "Unknown challenge, use " + parent + label + " to see available challenges");
					
					challenge.complete(player, island.getChallengeStorage());
					
					island.save();
					sender.sendMessage(Utilities.format("&6[Skyblock] &aYou have completed the &e%s &achallenge!", challenge.getName()));
				}
				catch(IllegalStateException e)
				{
					throw new IllegalArgumentException(e.getMessage());
				}
			}
			// Show info
			else
			{
				Island island = mManager.getIsland(player.getUniqueId());
				if (island == null)
					throw new IllegalArgumentException("You do not have an island. You need to create one first before you can see challenges.");
				
				Challenge challenge = mManager.getChallenges().getChallenge(args[0]);
				if (challenge == null)
					throw new BadArgumentException(0, "Unknown challenge, use " + parent + label + " to see available challenges");
				
				if (!island.getChallengeStorage().allComplete(challenge.getDependencies()))
					throw new IllegalStateException("You have not completed the required challenges");
				
				sender.sendMessage(Utilities.format("&6[Skyblock] &f%s challenge:", challenge.getName()));
				
				ImmutableList.Builder<String> description = ImmutableList.builder();
				challenge.addDescription(description, island.getChallengeStorage().isComplete(challenge));
				
				for (String line : description.build())
					sender.sendMessage(line);
			}
		}
		// List challenges
		else
		{
			Island island = mManager.getIsland(player.getUniqueId());
			if (island == null)
				throw new IllegalArgumentException("You do not have an island. You need to create one first before you can see challenges.");
			
			// Compute what challenges have been completed, and what can be completed
			LinkedList<Challenge> available = new LinkedList<Challenge>();
			LinkedList<Challenge> completed = new LinkedList<Challenge>();
			
			for (Challenge challenge : mManager.getChallenges().getChallenges())
			{
				if (island.getChallengeStorage().isComplete(challenge))
				{
					completed.add(challenge);
					if (challenge.isRepeatable() && System.currentTimeMillis() - island.getChallengeStorage().getCompletionTime(challenge) > challenge.getCooldownLength())
						available.add(challenge);
				}
				else
				{
					if (island.getChallengeStorage().allComplete(challenge.getDependencies()))
						available.add(challenge);
				}
			}
			
			// Display
			sender.sendMessage(Utilities.format("&6[Skyblock] &fChallenges:"));
			sender.sendMessage(Utilities.format("&7 You have completed &e%d &7of &e%d &7challenges:", completed.size(), mManager.getChallenges().getChallenges().size()));
			
			if (!completed.isEmpty())
			{
				sender.sendMessage(Utilities.format("&a Completed: "));
				StringBuilder builder = new StringBuilder();
				for (Challenge challenge : completed)
				{
					if (builder.length() != 0)
						builder.append(", ");
					
					if (challenge.isRepeatable() && available.contains(challenge))
					{
						builder.append(ChatColor.GOLD);
						builder.append(challenge.getName());
						builder.append(ChatColor.YELLOW);
					}
					else
						builder.append(challenge.getName());
				}
				sender.sendMessage(Utilities.format("  &e%s", builder));
			}
			
			if (!available.isEmpty())
			{
				sender.sendMessage(Utilities.format("&a Available:"));
				StringBuilder builder = new StringBuilder();
				for (Challenge challenge : available)
				{
					if (builder.length() != 0)
						builder.append(", ");
					builder.append(challenge.getName());
				}
				sender.sendMessage(Utilities.format("  &7%s", builder));
			}
		}
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		if (args.length == 1)
			return Monolith.matchStrings(args[0], Arrays.asList("complete", "c"));
		return null;
	}

}
