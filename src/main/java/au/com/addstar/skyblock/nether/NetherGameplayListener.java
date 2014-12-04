package au.com.addstar.skyblock.nether;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import au.com.addstar.skyblock.SkyblockManager;
import au.com.addstar.skyblock.SkyblockWorld;
import au.com.addstar.skyblock.island.Island;

public class NetherGameplayListener implements Listener
{
	private final Random mRand = new Random();
	private final String mHotWaterBucketName = ChatColor.WHITE + "Hot Water Bucket";
	private SkyblockManager mManager;
	
	public NetherGameplayListener(SkyblockManager manager)
	{
		mManager = manager;
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onPickupWater(PlayerBucketFillEvent event)
	{
		if (!mManager.getUsesNether() || event.getPlayer().getWorld().getEnvironment() != Environment.NETHER)
			return;
		
		SkyblockWorld world = mManager.getSkyblockWorld(event.getPlayer().getWorld());
		if (world == null)
			return;
		
		Block block = event.getBlockClicked().getRelative(event.getBlockFace());
		if (block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER)
		{
			ItemStack bucket = new ItemStack(Material.WATER_BUCKET);
			ItemMeta meta = bucket.getItemMeta();
			meta.setDisplayName(mHotWaterBucketName);
			meta.setLore(Arrays.asList(ChatColor.GRAY + "Can be placed in the nether"));
			bucket.setItemMeta(meta);
			event.setItemStack(bucket);
		}
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	public void onPlaceWater(PlayerBucketEmptyEvent event)
	{
		if (!mManager.getUsesNether() || event.getPlayer().getWorld().getEnvironment() != Environment.NETHER)
			return;
		
		SkyblockWorld world = mManager.getSkyblockWorld(event.getPlayer().getWorld());
		if (world == null)
			return;
		
		final Block block = event.getBlockClicked().getRelative(event.getBlockFace());
		final Player player = event.getPlayer();
		final ItemStack bucket = player.getItemInHand();
		
		if (bucket.getType() == Material.WATER_BUCKET && bucket.hasItemMeta() && mHotWaterBucketName.equals(bucket.getItemMeta().getDisplayName()))
		{
			event.setCancelled(true);
			Bukkit.getScheduler().runTask(mManager.getPlugin(), new Runnable()
			{
				@SuppressWarnings( "deprecation" )
				@Override
				public void run()
				{
					block.setType(Material.WATER);
					block.setData((byte)0);
					
					if (player.getGameMode() != GameMode.CREATIVE)
					{
						player.getInventory().removeItem(bucket);
						player.getInventory().addItem(new ItemStack(Material.BUCKET));
					}
				}
			});
		}
		
		Bukkit.getScheduler().runTask(mManager.getPlugin(), new PlaceChecker(block));
	}
	
	@EventHandler(ignoreCancelled=true)
	public void onBlockForm(BlockFromToEvent event)
	{
		if (!mManager.getUsesNether() || event.getBlock().getWorld().getEnvironment() != Environment.NETHER)
			return;
		
		SkyblockWorld world = mManager.getSkyblockWorld(event.getBlock().getWorld());
		if (world == null)
			return;
		
		Block from = event.getBlock();
		Block to = event.getToBlock();
		
		if (from.getType() == Material.LAVA || from.getType() == Material.STATIONARY_LAVA)
		{
			if (isAdjacent(to, Material.WATER, Material.STATIONARY_WATER))
				Bukkit.getScheduler().runTask(mManager.getPlugin(), new PlaceChecker(to));
		}
	}
	
	@EventHandler
	public void onBlockPhysics(BlockPhysicsEvent event)
	{
		if (!mManager.getUsesNether() || event.getBlock().getWorld().getEnvironment() != Environment.NETHER)
			return;
		
		SkyblockWorld world = mManager.getSkyblockWorld(event.getBlock().getWorld());
		if (world == null)
			return;
		
		Block block = event.getBlock();
		
		if (block.getType() == Material.LAVA)
		{
			if (isAdjacent(block, Material.WATER, Material.STATIONARY_WATER))
				Bukkit.getScheduler().runTask(mManager.getPlugin(), new PlaceChecker(block));
		}
	}
	
	private static final BlockFace[] mBlockFaces = new BlockFace[] {BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST};
	private boolean isAdjacent(Block block, Material... types)
	{
		for (BlockFace face : mBlockFaces)
		{
			Block other = block.getRelative(face);
			for (Material type : types)
			{
				if (other.getType() == type)
					return true;
			}
		}
		
		return false;
	}
	
	@EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
	public void onPortalUse(PlayerPortalEvent event)
	{
		Player player = event.getPlayer();
		if (!mManager.getUsesNether())
			return;
		
		SkyblockWorld world = mManager.getSkyblockWorld(player.getWorld());
		if (world == null)
			return;
		
		Island island = mManager.getIslandAt(event.getFrom(), true);
		
		if (island != null)
			event.setPortalTravelAgent(new PortalTravelAgent(event.getPortalTravelAgent(), island));
		
		if (event.getFrom().getWorld().getEnvironment() == Environment.NORMAL)
			event.setTo(new Location(world.getWorld(Environment.NETHER), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ()));
		else
			event.setTo(new Location(world.getWorld(Environment.NORMAL), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ()));
		
		event.useTravelAgent(true);
	}
	
	@EventHandler(priority=EventPriority.LOWEST, ignoreCancelled=true)
	public void onEntitySpawn(CreatureSpawnEvent event)
	{
		if (!mManager.getUsesNether() || event.getLocation().getWorld().getEnvironment() != Environment.NETHER)
			return;
		
		SkyblockWorld world = mManager.getSkyblockWorld(event.getLocation().getWorld());
		if (world == null)
			return;
		
		// Blaze, wither skeletons, and skeletons do not spawn in our nether as there are no nether fortresses. Allow them to spawn if certain conditions are met
		
		// Zombie pigmen are so common, so we will replace these sometimes
		if (event.getEntityType() != EntityType.PIG_ZOMBIE)
			return;
		
		
		Block block = event.getEntity().getLocation().getBlock().getRelative(BlockFace.DOWN);
		if (block.getType() == Material.NETHER_BRICK && block.getRelative(BlockFace.UP).getType().isTransparent() && block.getRelative(BlockFace.UP, 2).getType().isTransparent())
		{
			int level = block.getRelative(BlockFace.UP).getLightLevel();
			boolean canWither = block.getRelative(BlockFace.UP, 3).getType().isTransparent();
				
			if (mRand.nextDouble() < 0.14) // Something spawn chance
			{
				ArrayList<EntityType> valid = new ArrayList<EntityType>();
				
				if (level < 12)
				{
					for (int i = 0; i < 7; ++i)
						valid.add(EntityType.BLAZE);
				}
				if (level < 6)
				{
					for (int i = 0; i < 4; ++i)
					valid.add(EntityType.SKELETON);
					if (canWither)
						valid.add(EntityType.WITHER); // This will be replaced with a wither skeleton later
				}
				
				event.setCancelled(true);
				
				EntityType type = valid.get(mRand.nextInt(valid.size()));
				Entity entity;
				switch(type)
				{
				case WITHER:
				{
					entity = event.getLocation().getWorld().spawn(event.getLocation(), Skeleton.class);
					((Skeleton)entity).setSkeletonType(SkeletonType.WITHER);
					break;
				}
				case SKELETON:
				{
					entity = event.getLocation().getWorld().spawn(event.getLocation(), Skeleton.class);
					((Skeleton)entity).setSkeletonType(SkeletonType.NORMAL);
					break;
				}
				default:
					entity = event.getLocation().getWorld().spawnEntity(event.getLocation(), type);
					break;
				}
			}
					
		}
	}
	
	private static class PlaceChecker implements Runnable
	{
		private Block mBlock;
		private boolean[] mInitial;
		
		public PlaceChecker(Block block)
		{
			mBlock = block;
			mInitial = new boolean[7];
			for (int i = 0; i < 6; ++i)
				mInitial[i] = isSourceOk(block.getRelative(mBlockFaces[i]));
			mInitial[6] = isSourceOk(block);
		}
		
		public boolean isSourceOk(Block block)
		{
			return !block.getType().isSolid();
		}
		
		public boolean isDestOk(Block block)
		{
			return block.getType() == Material.COBBLESTONE || block.getType() == Material.STONE;
		}

		@Override
		public void run()
		{
			for (int i = 0; i <= 6; ++i)
			{
				if (!mInitial[i])
					continue;
				
				if (i < 6)
				{
					Block dest = mBlock.getRelative(mBlockFaces[i]);
					if (isDestOk(dest))
						dest.setType(Material.NETHERRACK);
				}
				else
				{
					if (isDestOk(mBlock))
						mBlock.setType(Material.NETHERRACK);
				}
			}
		}
	}
}
