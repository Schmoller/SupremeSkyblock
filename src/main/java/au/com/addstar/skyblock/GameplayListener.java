package au.com.addstar.skyblock;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.Dispenser;

import au.com.addstar.skyblock.challenge.Challenge;
import au.com.addstar.skyblock.challenge.ChallengeStorage;
import au.com.addstar.skyblock.challenge.types.CraftChallenge;
import au.com.addstar.skyblock.challenge.types.MobKillChallenge;
import au.com.addstar.skyblock.island.Island;
import au.com.addstar.skyblock.misc.Utilities;

public class GameplayListener implements Listener
{
	private SkyblockManager mManager;
	private HashMap<Player, Island> mLastIsland;
	
	public GameplayListener(SkyblockManager manager)
	{
		mManager = manager;
		mLastIsland = new HashMap<Player, Island>();
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
		
		if (event.getBlock().getY() == 0)
			event.setCancelled(true);
		else
		{
			Island island = mManager.getIslandAt(event.getBlock().getLocation());
			if (island != null)
				island.markScoreDirty();
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
	public void onBucketEmpty(PlayerBucketEmptyEvent event)
	{
		if (mManager.getSkyblockWorld(event.getBlockClicked().getWorld()) == null)
			return;
		
		Block block = event.getBlockClicked().getRelative(event.getBlockFace());
		if (block.getY() == 0)
			event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
	public void onPistonPush(BlockPistonExtendEvent event)
	{
		if (mManager.getSkyblockWorld(event.getBlock().getWorld()) == null)
			return;
		
		if (event.getDirection() != BlockFace.DOWN)
			return;
		
		for (Block block : event.getBlocks())
		{
			if (block.getY() <= 1)
			{
				event.setCancelled(true);
				break;
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
	public void onDispense(BlockDispenseEvent event)
	{
		if (mManager.getSkyblockWorld(event.getBlock().getWorld()) == null)
			return;
		
		if (event.getBlock().getY() != 1)
			return;
		
		Dispenser dispenser = (Dispenser)event.getBlock().getState().getData();
		if (dispenser.getFacing() == BlockFace.DOWN && event.getItem().getType() == Material.WATER_BUCKET || event.getItem().getType() == Material.LAVA_BUCKET)
			event.setCancelled(true);
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
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onCraft(CraftItemEvent event)
	{
		if (!(event.getWhoClicked() instanceof Player))
			return;
		
		final Player player = (Player)event.getWhoClicked();
		final Island island = mManager.getIslandAt(player.getLocation());
		if (island == null || !island.canAssist(player))
			return;
		
		if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)
		{
			// Shift click, craft all possible
			final ItemStack[] startInventory = Utilities.deepCopy(player.getInventory().getContents());
			final ItemStack craftItem = event.getCurrentItem();
			
			Bukkit.getScheduler().runTaskLater(mManager.getPlugin(), new Runnable()
			{
				@Override
				public void run()
				{
					ItemStack[] endInventory = player.getInventory().getContents();
					
					int count = 0;
					for (int i = 0; i < startInventory.length; i++)
					{
						ItemStack start = startInventory[i];
						ItemStack end = endInventory[i];

						if (start == null)
						{
							if (end != null && end.isSimilar(craftItem))
								count += end.getAmount();
						}
						else if (start.isSimilar(end) && end.isSimilar(craftItem))
							count += (end.getAmount() - start.getAmount());
					}
					
					ItemStack result = craftItem.clone();
					result.setAmount(count);
					
					recordCrafting(result, player, island.getChallengeStorage());
				}
			}, 1);
		}
		else if (event.getAction() == InventoryAction.PICKUP_ALL)
			recordCrafting(event.getCurrentItem(), player, island.getChallengeStorage());
	}
	
	private void recordCrafting(ItemStack item, Player player, ChallengeStorage storage)
	{
		for (Challenge challenge : mManager.getChallenges().getChallenges())
		{
			if (challenge instanceof CraftChallenge)
				((CraftChallenge) challenge).onItemCraft(item, player, storage);
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onEntityKill(EntityDeathEvent event)
	{
		if (event.getEntity().getKiller() == null)
			return;
		
		Player player = event.getEntity().getKiller();
		
		Island island = mManager.getIslandAt(event.getEntity().getLocation());
		if (island == null || !island.canAssist(player))
			return;
		
		ChallengeStorage storage = island.getChallengeStorage();
		
		for (Challenge challenge : mManager.getChallenges().getChallenges())
		{
			if (challenge instanceof MobKillChallenge)
				((MobKillChallenge) challenge).onEntityKill(event.getEntity(), player, storage);
		}
	}
	
	// Handle messages for changing islands
	
	@EventHandler
	public void onTeleport(PlayerTeleportEvent event)
	{
		updatePlayerIsland(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event)
	{
		updatePlayerIsland(event.getPlayer());
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		mLastIsland.remove(event.getPlayer());
	}
	
	private void updatePlayerIsland(Player player)
	{
		Island oldIsland = mLastIsland.get(player);
		Island newIsland = mManager.getIslandAt(player.getLocation());
		
		if (oldIsland != newIsland)
		{
			if (oldIsland != null && mManager.getIslandNotifyOnLeave())
				player.sendMessage(Utilities.format("&6[Skyblock] &eYou are leaving %s's island", oldIsland.getOwnerName()));
			if (newIsland != null && mManager.getIslandNotifyOnEnter())
				player.sendMessage(Utilities.format("&6[Skyblock] &eYou are entering %s's island", newIsland.getOwnerName()));
			
			mLastIsland.put(player, newIsland);
		}
	}
}
