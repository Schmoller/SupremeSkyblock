package au.com.addstar.skyblock.challenge.types;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableList.Builder;

import au.com.addstar.skyblock.challenge.Challenge;
import au.com.addstar.skyblock.challenge.ChallengeStorage;
import au.com.addstar.skyblock.challenge.ProgressionChallenge;
import au.com.addstar.skyblock.island.Island;
import au.com.addstar.skyblock.misc.Utilities;

public class MobKillChallenge extends Challenge implements ProgressionChallenge
{
	private Map<EntityType, Integer> mEntities;
	
	public MobKillChallenge(String name)
	{
		super(name);
		
		mEntities = Maps.newHashMap();
	}
	
	@Override
	public void load( ConfigurationSection section )
	{
		super.load(section);
		
		if (section.isList("entities"))
		{
			List<String> defs = section.getStringList("entities");
			for (String def : defs)
			{
				String[] parts = def.split(" ");
				if (parts.length != 2)
					throw new IllegalArgumentException("Error in entity definition " + def + ": Should be in the format of <type> <count>");
				
				EntityType type;
				try
				{
					type = EntityType.valueOf(parts[0].toUpperCase());
				}
				catch(IllegalArgumentException e)
				{
					throw new IllegalArgumentException("Error in entity definition " + def + ": Unknown entity type " + parts[1]);
				}
				
				int count;
				try
				{
					count = Integer.parseInt(parts[1]);
					if (count <= 0)
						throw new IllegalArgumentException("Error in entity definition " + def + ": Count must be 1 or more"); 
				}
				catch(NumberFormatException e)
				{
					throw new IllegalArgumentException("Error in entity definition " + def + ": Should be in the format of <type> <count>");
				}
				
				mEntities.put(type, count);
			}
		}
	}
	
	public Map<EntityType, Integer> getRemaining(ChallengeStorage storage)
	{
		Map<EntityType, Integer> types = Maps.newHashMap();
		
		ConfigurationSection section = storage.getExtra(this);
		for (EntityType type : mEntities.keySet())
		{
			int required = mEntities.get(type);
			if (!section.contains(type.name()))
				types.put(type, required);
			else
			{
				int count = section.getInt(type.name(), 0);
				if (count < required)
					types.put(type, required - count);
			}
		}
		
		return types;
	}
	
	private void recordEntity(Entity entity, ChallengeStorage storage)
	{
		ConfigurationSection section = storage.getExtra(this);
		EntityType type = entity.getType();
		
		if (!section.contains(type.name()))
			section.set(type.name(), 1);
		else
		{
			int count = section.getInt(type.name(), 0);
			section.set(type.name(), count+1);
		}
	}
	
	@Override
	protected void onComplete( Player player, Island island ) throws IllegalStateException
	{
		if (!getRemaining(island.getChallengeStorage()).isEmpty())
			throw new IllegalStateException("You have not killed all the creatures required for this challange");
	}

	@Override
	public boolean isManual()
	{
		return false;
	}

	@Override
	protected void addRequirementDescription( Builder<String> builder, boolean completed, ChallengeStorage storage )
	{
		if (completed)
		{
			builder.add(Utilities.format(" These creatures were killed:"));
			for (Entry<EntityType,Integer> item : mEntities.entrySet())
				builder.add(Utilities.format("&7 - &e%d&7x &e%s", item.getValue(), item.getKey().name()));
		}
		else
		{
			builder.add(Utilities.format(" You need to kill the following:"));
			for (Entry<EntityType,Integer> item : getRemaining(storage).entrySet())
				builder.add(Utilities.format("&7 - &e%d&7x &e%s", item.getValue(), item.getKey().name()));
		}
	}
	
	@Override
	public float attemptComplete( Player player, ChallengeStorage storage ) throws IllegalStateException
	{
		throw new IllegalStateException("Not a manual completion");
	}
	
	@Override
	public float getProgress( ChallengeStorage storage )
	{
		return 0;
	}
	
	public void onEntityKill(Entity entity, Player player, ChallengeStorage storage)
	{
		try
		{
			checkCanComplete(storage);
		}
		catch(IllegalStateException e)
		{
			return;
		}
		
		boolean madeProgress = false;
		for (EntityType type : getRemaining(storage).keySet())
		{
			if (entity.getType() == type)
			{
				recordEntity(entity, storage);
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

}
