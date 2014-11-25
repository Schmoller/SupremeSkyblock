package au.com.addstar.skyblock;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import au.com.addstar.skyblock.island.Island;
import au.com.addstar.skyblock.misc.Utilities;

public class GameplayListener implements Listener
{
	private SkyblockManager mManager;
	
	public GameplayListener(SkyblockManager manager)
	{
		mManager = manager;
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onRespawn(PlayerRespawnEvent event)
	{
		Player player = event.getPlayer();
		if (mManager.getSkyblockWorld(player.getWorld()) == null)
			return;
		
		Island island = mManager.getIslandAt(player.getLocation());
		if (island == null || !island.canAssist(player))
		{
			island = mManager.getIsland(player.getUniqueId());
			// Let the default respawn logic take hold if they have no island and cannot assist with this island
			if (island == null)
				return;
		}
		
		event.setRespawnLocation(island.getIslandSpawn());
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event)
	{
		Player player = event.getPlayer();
		if (mManager.getSkyblockWorld(player.getWorld()) == null)
			return;
		
		Island island = mManager.getIslandAt(event.getBlock().getLocation());
		if (island != null)
			island.markScoreDirty();
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockRemove(BlockBreakEvent event)
	{
		Player player = event.getPlayer();
		if (mManager.getSkyblockWorld(player.getWorld()) == null)
			return;
		
		Island island = mManager.getIslandAt(event.getBlock().getLocation());
		if (island != null)
			island.markScoreDirty();
	}
	
	@SuppressWarnings( "deprecation" )
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled = true)
	public void onInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		if (mManager.getSkyblockWorld(player.getWorld()) == null)
			return;
		
		Island island;
		if (event.getClickedBlock() != null)
			island = mManager.getIslandAt(event.getClickedBlock().getLocation());
		else
			island = mManager.getIslandAt(player.getLocation());
		
		if (island != null && island.canAssist(player))
		{
			island.setLastUseTime(System.currentTimeMillis());
			
			// Handle reversing water/lava
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
			{
				if (event.getItem() != null && event.getItem().getType() == Material.BUCKET)
				{
					PlayerInventory inventory = event.getPlayer().getInventory();
					
					// Reverse obsidian into lava
					if (event.getClickedBlock().getType() == Material.OBSIDIAN && mManager.getPlayerCanReverseLava())
					{
						// Confirm that they could have picked the block up
						if (!event.getPlayer().getTargetBlock(null, 10).equals(event.getClickedBlock()))
							return;
						
						ItemStack item = new ItemStack(Material.LAVA_BUCKET);
						ItemStack old = new ItemStack(Material.BUCKET);
						
						if (inventory.removeItem(old).isEmpty())
						{
							if (!inventory.addItem(item).isEmpty())
							{
								// No room for the lava bucket
								inventory.addItem(old);
								event.getPlayer().sendMessage(Utilities.format("&6[Skyblock] &cYou don't have any room in your inventory for the lava bucket"));
							}
							else
							{
								event.getClickedBlock().setType(Material.AIR);
								event.getPlayer().updateInventory();
								event.getPlayer().sendMessage(Utilities.format("&6[Skyblock] &eThere you go, be careful next time."));
							}
						}
					}
					
					// Reverse cobble/stone into water
					if ((event.getClickedBlock().getType() == Material.STONE || event.getClickedBlock().getType() == Material.COBBLESTONE) && mManager.getPlayerCanReverseWater())
					{
						// Confirm that they could have picked the block up
						if (!event.getPlayer().getTargetBlock(null, 10).equals(event.getClickedBlock()))
							return;
						
						ItemStack item = new ItemStack(Material.WATER_BUCKET);
						ItemStack old = new ItemStack(Material.BUCKET);
						
						if (inventory.removeItem(old).isEmpty())
						{
							if (!inventory.addItem(item).isEmpty())
							{
								// No room for the lava bucket
								inventory.addItem(old);
								event.getPlayer().sendMessage(Utilities.format("&6[Skyblock] &cYou don't have any room in your inventory for the water bucket"));
							}
							else
							{
								event.getClickedBlock().setType(Material.AIR);
								event.getPlayer().updateInventory();
								event.getPlayer().sendMessage(Utilities.format("&6[Skyblock] &eThere you go, be careful next time."));
							}
						}
					}
				}
			}
		}
	}
}
