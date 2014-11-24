package au.com.addstar.skyblock.command;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.addstar.monolith.command.BadArgumentException;
import au.com.addstar.monolith.command.CommandSenderType;
import au.com.addstar.monolith.command.ICommand;
import au.com.addstar.skyblock.SkyblockManager;
import au.com.addstar.skyblock.island.Island;
import au.com.addstar.skyblock.misc.Callback;
import au.com.addstar.skyblock.misc.ConfirmationPrompt;
import au.com.addstar.skyblock.misc.Utilities;

public class TransferCommand implements ICommand
{
	private SkyblockManager mManager;
	
	public TransferCommand(SkyblockManager manager)
	{
		mManager = manager;
	}
	
	@Override
	public String getName()
	{
		return "transfer";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {"makeleader"};
	}

	@Override
	public String getPermission()
	{
		return "skyblock.command.transfer";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <player>";
	}

	@Override
	public String getDescription()
	{
		return "Transfers an island to another player";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		if (args.length != 1)
			return false;
		
		final Player player = (Player)sender;
		final Island island = mManager.getIsland(((Player)sender).getUniqueId());
		
		if (island == null)
			throw new IllegalArgumentException("You do not have an island to transfer");
		
		final Player dest = Bukkit.getPlayer(args[0]);
		
		if (dest == null)
			throw new BadArgumentException(0, "Unknown player");
		
		new ConfirmationPrompt()
			.setPlayer(player)
			.setText(Utilities.format("&6[Skyblock] &fAre you sure you want to transfer your island to %s?", dest.getDisplayName()))
			.setCallback(new Callback()
			{
				@Override
				public void onComplete( boolean success, Throwable exception )
				{
					if (!success)
						player.sendMessage(Utilities.format("&6[Skyblock] &eTransfer cancelled"));
					else
					{
						final Island theirIsland = mManager.getIsland(dest.getUniqueId());
						if (theirIsland == null)
						{
							new ConfirmationPrompt()
							.setPlayer(dest)
							.setText(Utilities.format("&6[Skyblock] &e%s wants to transfer their island to you. Do you accept?", player.getDisplayName()))
							.setCallback(new Callback()
							{
								@Override
								public void onComplete( boolean success, Throwable exception )
								{
									if (!success)
										player.sendMessage(Utilities.format("&6[Skyblock] &c%s rejected your offer to transfer your island.", dest.getDisplayName()));
									else
									{
										// Last minute check in case something has changed since
										if (!island.getOwner().equals(player.getUniqueId()))
											dest.sendMessage(Utilities.format("&6[Skyblock] &cUnable to accept. %s no longer owns that island.", player.getDisplayName()));
										else
										{
											island.setOwner(dest);
											mManager.save();
											player.sendMessage(Utilities.format("&6[Skyblock] &eYour island has been transferred to %s", dest.getDisplayName()));
											dest.sendMessage(Utilities.format("&6[Skyblock] &aYou now own %s's island. Use &e/is home&a to go there"));
										}
									}
								}
							})
							.launch();
						}
						else
						{
							new ConfirmationPrompt()
							.setPlayer(dest)
							.setText(Utilities.format("&6[Skyblock] &e%s wants to transfer their island to you. Do you accept?\n&cWARNING! &eYou already own an island. You will &llose&e ownership of it if you accept", player.getDisplayName()))
							.setCallback(new Callback()
							{
								@Override
								public void onComplete( boolean success, Throwable exception )
								{
									if (!success)
										player.sendMessage(Utilities.format("&6[Skyblock] &c%s rejected your offer to transfer your island.", dest.getDisplayName()));
									else
									{
										// Last minute check in case something has changed since
										if (!island.getOwner().equals(player.getUniqueId()))
											dest.sendMessage(Utilities.format("&6[Skyblock] &cUnable to accept. %s no longer owns that island.", player.getDisplayName()));
										else
										{
											// If there are members, a new owner for the island will have to be found or we cannot continue.
											if (!theirIsland.getMembers().isEmpty())
											{
												UUID newOwner = null;
												// Try to find a new owner
												for (UUID member : theirIsland.getMembers())
												{
													Island memberIsland = mManager.getIsland(member);
													if (memberIsland == null)
													{
														newOwner = member;
														break;
													}
												}
												
												if (newOwner != null)
												{
													theirIsland.setOwnerByMember(newOwner);
													Player memberPlayer = Bukkit.getPlayer(newOwner);
													if (memberPlayer != null)
														player.sendMessage(Utilities.format("&6[Skyblock] &eYou have been made the owner of %s's island. %s moved %s's island.", dest.getDisplayName(), dest.getDisplayName(), player.getDisplayName()));
												}
												else
												{
													dest.sendMessage(Utilities.format("&6[Skyblock] &cUnable to accept. You have %d members on your island and none can accept ownership of your island.", theirIsland.getMembers().size()));
													player.sendMessage(Utilities.format("&6[Skyblock] &c%s was unable to accept your request. Their island could not be transfered to one of its members", dest.getDisplayName()));
													return;
												}
											}
											else
												theirIsland.abandonIsland();
											
											island.setOwner(dest);
											mManager.save();
											player.sendMessage(Utilities.format("&6[Skyblock] &eYour island has been transferred to %s", dest.getDisplayName()));
											dest.sendMessage(Utilities.format("&6[Skyblock] &aYou now own %s's island. Use &e/is home&a to go there", player.getDisplayName()));
										}
									}
								}
							})
							.launch();
						}
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
