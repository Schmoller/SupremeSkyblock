package au.com.addstar.skyblock.challenge.types;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.google.common.collect.ImmutableList.Builder;

import au.com.addstar.monolith.StringTranslator;
import au.com.addstar.skyblock.challenge.Challenge;
import au.com.addstar.skyblock.challenge.ChallengeStorage;
import au.com.addstar.skyblock.challenge.ProgressionChallenge;
import au.com.addstar.skyblock.island.Island;
import au.com.addstar.skyblock.misc.Utilities;

public class CraftChallenge extends Challenge implements ProgressionChallenge
{
	private List<ItemStack> mItems;
	
	public CraftChallenge(String name)
	{
		super(name);
		
		mItems = new ArrayList<ItemStack>();
	}
	
	@Override
	protected void onComplete( Player player, Island island ) throws IllegalStateException
	{
		if (!getRemaining(island.getChallengeStorage()).isEmpty())
			throw new IllegalStateException("You have not crafted all the items required for this challange");
	}
	
	@Override
	protected void addRequirementDescription( Builder<String> builder, boolean completed, ChallengeStorage storage )
	{
		if (completed)
		{
			builder.add(Utilities.format(" The following items had to be crafted:"));
			for (ItemStack item : mItems)
				builder.add(Utilities.format("&7 - &e%d&7x &e%s", item.getAmount(), StringTranslator.getName(item)));
		}
		else
		{
			builder.add(Utilities.format(" The following items must be crafted:"));
			for (ItemStack item : getRemaining(storage))
				builder.add(Utilities.format("&7 - &e%d&7x &e%s", item.getAmount(), StringTranslator.getName(item)));
		}
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
		return false;
	}

	public List<ItemStack> getRequiredItems()
	{
		return mItems;
	}

	@Override
	public float getProgress( ChallengeStorage storage )
	{
		int total = 0;
		int complete = 0;
		ConfigurationSection section = storage.getExtra(this);
		
		for (ItemStack item : mItems)
		{
			total += item.getAmount();
			String name = String.format("%s-%d", item.getType().name(), item.getDurability());
			
			if (section.contains(name))
			{
				int count = section.getInt(name, 0);
				if (count < item.getAmount())
					complete += count;
				else
					complete += item.getAmount();
			}
		}
		
		return (float)complete/(float)total;
	}
	
	public List<ItemStack> getRemaining(ChallengeStorage storage)
	{
		ArrayList<ItemStack> items = new ArrayList<ItemStack>(mItems.size());
		ConfigurationSection section = storage.getExtra(this);
		
		for (ItemStack item : mItems)
		{
			String name = String.format("%s-%d", item.getType().name(), item.getDurability());
			
			if (!section.contains(name))
				items.add(item);
			else
			{
				int count = section.getInt(name, 0);
				if (count < item.getAmount())
				{
					ItemStack crafted = item.clone();
					crafted.setAmount(item.getAmount() - count);
					items.add(crafted);
				}
			}
		}
		
		return items;
	}
	
	private void recordItem(ItemStack item, ChallengeStorage storage)
	{
		ConfigurationSection section = storage.getExtra(this);
		
		String name = String.format("%s-%d", item.getType().name(), item.getDurability());
		
		if (!section.contains(name))
			section.set(name, item.getAmount());
		else
		{
			int count = section.getInt(name, 0);
			count += item.getAmount();
			section.set(name, count);
		}
	}
	
	public void onItemCraft( ItemStack crafted, Player player, ChallengeStorage storage )
	{
		try
		{
			checkCanComplete(storage);
		}
		catch(IllegalStateException e)
		{
			return;
		}
		
		List<ItemStack> items = getRemaining(storage);
		boolean madeProgress = false;
		for (ItemStack item : items)
		{
			if (item.isSimilar(crafted))
			{
				recordItem(crafted, storage);
				madeProgress = true;
				break;
			}
		}
		
		if (getRemaining(storage).isEmpty())
		{
			complete(player, storage);
			player.sendMessage(Utilities.format("&6[Skyblock] &aYou have completed the &e%s &achallenge!", getName()));
		}
		
		if (madeProgress)
			storage.getIsland().save();
	}

	@Override
	public float attemptComplete( Player player, ChallengeStorage storage ) throws IllegalStateException
	{
		throw new IllegalStateException("Not a manual completion");
	}
}

