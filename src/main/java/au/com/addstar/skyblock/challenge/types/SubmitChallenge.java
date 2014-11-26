package au.com.addstar.skyblock.challenge.types;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.google.common.collect.ImmutableList.Builder;

import au.com.addstar.monolith.StringTranslator;
import au.com.addstar.skyblock.challenge.ChallengeStorage;
import au.com.addstar.skyblock.island.Island;
import au.com.addstar.skyblock.misc.Utilities;

public class SubmitChallenge extends DetectChallenge
{
	public SubmitChallenge(String name)
	{
		super(name);
	}
	
	private void removeItems(Player player)
	{
		PlayerInventory inventory = player.getInventory();
		for(ItemStack item : getRequiredItems())
			inventory.removeItem(item);
	}
	
	@Override
	protected void onComplete( Player player, Island island ) throws IllegalStateException
	{
		super.onComplete(player, island);
		
		removeItems(player);
	}
	
	@Override
	protected void addRequirementDescription( Builder<String> builder, boolean completed, ChallengeStorage storage )
	{
		if (completed)
			builder.add(Utilities.format(" The following items were required:"));
		else
			builder.add(Utilities.format(" The following items are required:"));
		for (ItemStack item : getRequiredItems())
			builder.add(Utilities.format("&7 - &e%d&7x &e%s", item.getAmount(), StringTranslator.getName(item)));
	}
}
