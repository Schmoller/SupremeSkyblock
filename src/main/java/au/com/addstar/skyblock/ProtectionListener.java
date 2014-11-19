package au.com.addstar.skyblock;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

import au.com.addstar.skyblock.island.Island;
import au.com.addstar.skyblock.misc.Utilities;

public class ProtectionListener implements Listener
{
	private SkyblockManager mManager;
	
	public ProtectionListener(SkyblockManager manager)
	{
		mManager = manager;
	}
	
	private boolean hasBypass(Player player)
	{
		return player.hasPermission("skyblock.protection.bypass");
	}
	
	private void notify(Player player)
	{
		player.sendMessage(Utilities.format("&6[Skyblock] &fYou dont have permission to touch this island"));
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onPlaceBlock(BlockPlaceEvent event)
	{
		Player player = event.getPlayer();
		if (hasBypass(player))
			return;
		
		Island island = mManager.getIslandAt(event.getBlock().getLocation());
		if (island == null)
			return;
		
		if (!island.canAssist(player))
		{
			event.setBuild(false);
			event.setCancelled(true);
			notify(player);
		}
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onPlaceBreak(BlockBreakEvent event)
	{
		Player player = event.getPlayer();
		if (hasBypass(player))
			return;
		
		Island island = mManager.getIslandAt(event.getBlock().getLocation());
		if (island == null)
			return;

		if (!island.canAssist(player))
		{
			event.setCancelled(true);
			notify(player);
		}
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onBedEnter(PlayerBedEnterEvent event)
	{
		Player player = event.getPlayer();
		if (hasBypass(player))
			return;
		
		Island island = mManager.getIslandAt(event.getBed().getLocation());
		if (island == null)
			return;

		if (!island.canAssist(player))
		{
			event.setCancelled(true);
			notify(player);
		}
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onPlaceBucket(PlayerBucketEmptyEvent event)
	{
		Player player = event.getPlayer();
		if (hasBypass(player))
			return;
		
		Island island = mManager.getIslandAt(event.getBlockClicked().getLocation());
		if (island == null)
			return;

		if (!island.canAssist(player))
		{
			event.setCancelled(true);
			notify(player);
		}
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onFillBucket(PlayerBucketFillEvent event)
	{
		Player player = event.getPlayer();
		if (hasBypass(player))
			return;
		
		Island island = mManager.getIslandAt(event.getBlockClicked().getLocation());
		if (island == null)
			return;

		if (!island.canAssist(player))
		{
			event.setCancelled(true);
			notify(player);
		}
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onDropItem(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();
		if (hasBypass(player))
			return;
		
		Island island = mManager.getIslandAt(player.getLocation());
		if (island == null)
			return;

		if (!island.canAssist(player))
		{
			event.setCancelled(true);
			notify(player);
		}
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onFish(PlayerFishEvent event)
	{
		Player player = event.getPlayer();
		if (hasBypass(player))
			return;
		
		Island island = mManager.getIslandAt((event.getHook() != null ? event.getHook().getLocation() : player.getLocation()));
		if (island == null)
			return;

		if (!island.canAssist(player))
		{
			event.setCancelled(true);
			notify(player);
		}
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		if (hasBypass(player) || event.getClickedBlock() == null)
			return;
		
		Island island = mManager.getIslandAt(event.getClickedBlock().getLocation());
		if (island == null)
			return;

		if (!island.canAssist(player))
		{
			event.setCancelled(true);
			notify(player);
		}
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onPickupItem(PlayerPickupItemEvent event)
	{
		Player player = event.getPlayer();
		if (hasBypass(player))
			return;
		
		Island island = mManager.getIslandAt(event.getItem().getLocation());
		if (island == null)
			return;

		if (!island.canAssist(player))
		{
			event.setCancelled(true);
			notify(player);
		}
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onShear(PlayerShearEntityEvent event)
	{
		Player player = event.getPlayer();
		if (hasBypass(player))
			return;
		
		Island island = mManager.getIslandAt(event.getEntity().getLocation());
		if (island == null)
			return;

		if (!island.canAssist(player))
		{
			event.setCancelled(true);
			notify(player);
		}
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onLeash(PlayerLeashEntityEvent event)
	{
		Player player = event.getPlayer();
		if (hasBypass(player))
			return;
		
		Island island = mManager.getIslandAt(event.getEntity().getLocation());
		if (island == null)
			return;

		if (!island.canAssist(player))
		{
			event.setCancelled(true);
			notify(player);
		}
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onHangingPlace(HangingPlaceEvent event)
	{
		Player player = event.getPlayer();
		if (hasBypass(player))
			return;
		
		Island island = mManager.getIslandAt(event.getBlock().getLocation());
		if (island == null)
			return;

		if (!island.canAssist(player))
		{
			event.setCancelled(true);
			notify(player);
		}
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onHangingBreak(HangingBreakByEntityEvent event)
	{
		if (!(event.getRemover() instanceof Player))
			return;
		
		Player player = (Player)event.getRemover();
		if (hasBypass(player))
			return;
		
		Island island = mManager.getIslandAt(event.getEntity().getLocation());
		if (island == null)
			return;

		if (!island.canAssist(player))
		{
			event.setCancelled(true);
			notify(player);
		}
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onVehicleEnter(VehicleEnterEvent event)
	{
		if (!(event.getEntered() instanceof Player))
			return;
		
		Player player = (Player)event.getEntered();
		if (hasBypass(player))
			return;
		
		Island island = mManager.getIslandAt(event.getVehicle().getLocation());
		if (island == null)
			return;

		if (!island.canAssist(player))
		{
			event.setCancelled(true);
			notify(player);
		}
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onVehicleDamage(VehicleDamageEvent event)
	{
		if (!(event.getAttacker() instanceof Player))
			return;
		
		Player player = (Player)event.getAttacker();
		if (hasBypass(player))
			return;
		
		Island island = mManager.getIslandAt(event.getVehicle().getLocation());
		if (island == null)
			return;

		if (!island.canAssist(player))
		{
			event.setCancelled(true);
			notify(player);
		}
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onEntityDamage(EntityDamageByEntityEvent event)
	{
		if (!(event.getDamager() instanceof Player))
			return;
		
		Player player = (Player)event.getDamager();
		if (hasBypass(player))
			return;
		
		Island island = mManager.getIslandAt(event.getEntity().getLocation());
		if (island == null)
			return;

		if (!island.canAssist(player))
		{
			event.setCancelled(true);
			notify(player);
		}
	}
}
