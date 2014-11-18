package au.com.addstar.skyblock.command;

import java.io.File;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.Region;

import au.com.addstar.monolith.command.BadArgumentException;
import au.com.addstar.monolith.command.CommandSenderType;
import au.com.addstar.monolith.command.ICommand;
import au.com.addstar.skyblock.SkyblockManager;
import au.com.addstar.skyblock.island.IslandTemplate;

public class TemplateCommand implements ICommand
{
	private static final Pattern validation = Pattern.compile("[a-zA-Z0-9_\\-]+");
	
	private File mBase;
	
	public TemplateCommand(SkyblockManager manager)
	{
		mBase = new File(manager.getPlugin().getDataFolder(), "templates");
	}
	
	@Override
	public String getName()
	{
		return "template";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "skyblock.commands.template";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " save <name>";
	}

	@Override
	public String getDescription()
	{
		return "Saves an island template from your current WorldEdit selection";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		Plugin raw = Bukkit.getPluginManager().getPlugin("WorldEdit");
		if (raw == null)
			throw new IllegalArgumentException("WorldEdit is not enabled on this server. This feature requires WorldEdit to function.");
		
		if (args.length != 2)
			return false;
		
		if (!args[0].equalsIgnoreCase("save"))
			throw new BadArgumentException(0, "Expected 'save'");
		
		String name = args[1];
		Matcher matcher = validation.matcher(name);
		if (!matcher.matches())
			throw new BadArgumentException(1, "Illegal charaters in name. Can only be a-z A-Z 0-9 _ or -");
		
		File destination = new File(mBase, name + ".template");
		
		Player player = (Player)sender;
		
		WorldEditPlugin plugin = (WorldEditPlugin)raw;
		
		BukkitPlayer wplayer = plugin.wrapPlayer(player);
		LocalSession session = plugin.getSession(player);
		
		if (session.isSelectionDefined(wplayer.getWorld()))
		{
			try
			{
				Region region = session.getSelection(wplayer.getWorld());
				IslandTemplate template = new IslandTemplate();
				template.load(region, player.getLocation());
				
				if (!mBase.exists())
					mBase.mkdirs();
				
				if (template.save(destination))
				{
					player.sendMessage(ChatColor.GREEN + "Template has been saved. The spawn of the island was set to the location you were standing in.");
					player.sendMessage(ChatColor.GRAY + "To use this template, enter the name '" + name + "' into the main config under the setting 'island.template:'");
				}
				else
					player.sendMessage(ChatColor.RED + "An error occured while saving the template. Please check the console.");
			}
			catch ( IncompleteRegionException e )
			{
				// Shouldnt happen
				throw new AssertionError(e);
			}
		}
		else
			throw new IllegalArgumentException("Please select an area with WorldEdit first.");
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}
}
