package au.com.addstar.skyblock.challenge;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;

import au.com.addstar.skyblock.island.Island;

public class ChallengeStorage
{
	private Island mIsland;
	
	private HashMap<Challenge, Long> mCompleted;
	private boolean mNeedsSaving;
	
	public ChallengeStorage(Island island)
	{
		mIsland = island;
		mCompleted = new HashMap<Challenge, Long>();
		mNeedsSaving = false;
	}
	
	public boolean isComplete(Challenge challenge)
	{
		return mCompleted.containsKey(challenge);
	}
	
	public boolean allComplete(Collection<Challenge> challenges)
	{
		for (Challenge challange : challenges)
		{
			if (!isComplete(challange))
				return false;
		}
		
		return true;
	}
	
	public void markComplete(Challenge challenge)
	{
		mCompleted.put(challenge, System.currentTimeMillis());
		mNeedsSaving = true;
	}
	
	public long getCompletionTime(Challenge challenge)
	{
		if (mCompleted.containsKey(challenge))
			return mCompleted.get(challenge);
		return 0;
	}
	
	public Island getIsland()
	{
		return mIsland;
	}
	
	public boolean needsSaving()
	{
		return mNeedsSaving;
	}
	
	public void save(ConfigurationSection dest)
	{
		ConfigurationSection section = dest.createSection("challenges");
		
		for (Entry<Challenge, Long> entry : mCompleted.entrySet())
			section.set(entry.getKey().getName(), entry.getValue());
		
		mNeedsSaving = false;
	}
	
	public void load(ConfigurationSection source)
	{
		mCompleted.clear();

		ConfigurationSection section = source.getConfigurationSection("challenges");
		if (section == null)
			return;
		
		for (String name : section.getKeys(false))
		{
			long date = section.getLong(name, System.currentTimeMillis());
			Challenge challenge = mIsland.getWorld().getManager().getChallenges().getChallenge(name);
			if (challenge != null)
				mCompleted.put(challenge, date);
		}
	}
}
