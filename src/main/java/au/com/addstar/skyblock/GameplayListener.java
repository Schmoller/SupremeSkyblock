package au.com.addstar.skyblock;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import au.com.addstar.skyblock.island.Island;

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
			island.setLastUseTime(System.currentTimeMillis());
	}
}
