package au.com.addstar.skyblock.challenge.types;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableList.Builder;

import au.com.addstar.monolith.StringTranslator;
import au.com.addstar.skyblock.challenge.Challenge;
import au.com.addstar.skyblock.challenge.ChallengeStorage;
import au.com.addstar.skyblock.challenge.IslandBasedChallenge;
import au.com.addstar.skyblock.island.Island;
import au.com.addstar.skyblock.misc.Utilities;

public class DetectIslandChallenge extends Challenge implements IslandBasedChallenge
{
	private Map<Material, Integer> mTypes;
	
	public DetectIslandChallenge(String name)
	{
		super(name);
		
		mTypes = Maps.newHashMap();
	}
	
	private boolean hasTypes(int[] typeCounts)
	{
		for(Entry<Material,Integer> type : mTypes.entrySet())
		{
			if (typeCounts[type.getKey().ordinal()] < type.getValue())
				return false;
		}
		
		return true;
	}
	
	@Override
	protected void onComplete( Player player, Island island ) throws IllegalStateException
	{
	}
	
	@Override
	protected void addRequirementDescription( Builder<String> builder, boolean completed, ChallengeStorage storage )
	{
		if (completed)
			builder.add(Utilities.format(" The following blocks had to be on your island:"));
		else
			builder.add(Utilities.format(" You need the following blocks on your island:"));
		
		for (Entry<Material,Integer> type : mTypes.entrySet())
		{
			ItemStack item = new ItemStack(type.getKey());
			builder.add(Utilities.format("&7 - &e%d&7x &e%s", type.getValue(), StringTranslator.getName(item)));
		}
	}
	
	@Override
	public void onCalculateScore(Island island, int[] typeCounts)
	{
		try
		{
			checkCanComplete(island.getChallengeStorage());
		}
		catch(IllegalStateException e)
		{
			return;
		}
		
		if (!hasTypes(typeCounts))
			return;
		
		Player player = Bukkit.getPlayer(island.getOwner());
		if (player == null)
			return;
		
		complete(player, island.getChallengeStorage());
		player.sendMessage(Utilities.format("&6[Skyblock] &aYou have completed the &e%s &achallenge!", getName()));
	
		island.save();
	}
	
	@Override
	public void load( ConfigurationSection section )
	{
		super.load(section);
		
		if (section.isList("types"))
		{
			List<String> defs = section.getStringList("types");
			mTypes = Maps.newHashMap();
			
			for(String def : defs)
			{
				ItemStack item = Utilities.parseItem(def.split(" "));
				if (!item.getType().isBlock())
					throw new IllegalArgumentException("Item is not a block type");
				mTypes.put(item.getType(), item.getAmount());
			}
		}
	}

	@Override
	public boolean isManual()
	{
		return false;
	}
}
