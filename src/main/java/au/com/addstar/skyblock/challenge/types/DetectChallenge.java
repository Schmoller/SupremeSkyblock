package au.com.addstar.skyblock.challenge.types;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.google.common.collect.ImmutableList.Builder;

import au.com.addstar.monolith.StringTranslator;
import au.com.addstar.skyblock.challenge.Challenge;
import au.com.addstar.skyblock.island.Island;
import au.com.addstar.skyblock.misc.Utilities;

public class DetectChallenge extends Challenge
{
	private List<ItemStack> mItems;
	
	public DetectChallenge(String name)
	{
		super(name);
		
		mItems = new ArrayList<ItemStack>();
	}
	
	private boolean hasItems(Player player)
	{
		PlayerInventory inventory = player.getInventory();
		for(ItemStack item : mItems)
		{
			if (!inventory.containsAtLeast(item, item.getAmount()))
				return false;
		}
		
		return true;
	}
	
	@Override
	protected void onComplete( Player player, Island island ) throws IllegalStateException
	{
		if (!hasItems(player))
			throw new IllegalStateException("You do not have all the items required for this challange");
	}
	
	@Override
	protected void addRequirementDescription( Builder<String> builder, boolean completed )
	{
		if (completed)
			builder.add(Utilities.format(" The following items had to be present:"));
		else
			builder.add(Utilities.format(" The following items must be present:"));
		
		for (ItemStack item : mItems)
			builder.add(Utilities.format("&7 - &e%d&7x &e%s", item.getAmount(), StringTranslator.getName(item)));
	}

	@Override
	public boolean isManual()
	{
		return true;
	}

	public List<ItemStack> getRequiredItems()
	{
		return mItems;
	}
}
