package au.com.addstar.skyblock.nether;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import au.com.addstar.skyblock.SkyblockManager;
import au.com.addstar.skyblock.SkyblockWorld;

public class NetherGameplayListener implements Listener
{
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
