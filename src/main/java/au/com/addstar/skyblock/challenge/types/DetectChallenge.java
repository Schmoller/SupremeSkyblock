package au.com.addstar.skyblock.challenge.types;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.google.common.collect.ImmutableList.Builder;

import au.com.addstar.monolith.StringTranslator;
import au.com.addstar.skyblock.challenge.Challenge;
import au.com.addstar.skyblock.challenge.ChallengeStorage;
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
	protected void addRequirementDescription( Builder<String> builder, boolean completed, ChallengeStorage storage )
	{
		if (completed)
			builder.add(Utilities.format(" The following items had to be present:"));
		else
			builder.add(Utilities.format(" The following items must be present:"));
		
		for (ItemStack item : mItems)
			builder.add(Utilities.format("&7 - &e%d&7x &e%s", item.getAmount(), StringTranslator.getName(item)));
	}
	
	@Override
	public void load( ConfigurationSection section )
	{
		super.load(section);
		
		if (section.isList("items"))
		{
			List<String> defs = section.getStringList("items");
			mItems = new ArrayList<ItemStack>(defs.size());
			
			for(String def : defs)
				mItems.add(Utilities.parseItem(def.split(" ")));
		}
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
