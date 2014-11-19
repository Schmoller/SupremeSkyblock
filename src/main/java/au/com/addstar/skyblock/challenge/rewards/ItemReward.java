package au.com.addstar.skyblock.challenge.rewards;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import au.com.addstar.monolith.StringTranslator;
import au.com.addstar.skyblock.misc.Utilities;

public class ItemReward extends Reward
{
	private ItemStack mItem;
	
	public ItemReward(ItemStack item)
	{
		mItem = item;
	}
	
	@Override
	public void apply( Player player )
	{
		Map<Integer, ItemStack> remaining = player.getInventory().addItem(mItem.clone());
		if (!remaining.isEmpty())
		{
			ItemStack remain = remaining.get(0);
			player.getWorld().dropItemNaturally(player.getLocation(), remain);
		}
	}
	
	@Override
	public String getName()
	{
		return Utilities.format("&e%d&7x &e%s", mItem.getAmount(), StringTranslator.getName(mItem));
	}

}
