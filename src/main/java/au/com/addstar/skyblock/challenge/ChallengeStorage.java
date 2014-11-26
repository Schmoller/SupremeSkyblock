package au.com.addstar.skyblock.challenge;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import au.com.addstar.skyblock.island.Island;

public class ChallengeStorage
{
	private Island mIsland;
	
	private ConfigurationSection mExtraRoot;
	private HashMap<Challenge, ConfigurationSection> mExtra;
	private HashMap<Challenge, Long> mCompleted;
	private boolean mNeedsSaving;
	
	public ChallengeStorage(Island island)
	{
		mIsland = island;
		mCompleted = new HashMap<Challenge, Long>();
		mExtra = new HashMap<Challenge, ConfigurationSection>();
		mExtraRoot = new YamlConfiguration();
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
	
	public ConfigurationSection getExtra(Challenge challenge)
	{
		if (mExtra.containsKey(challenge))
			return mExtra.get(challenge);
		
		ConfigurationSection section = mExtraRoot.createSection(challenge.getName());
		mExtra.put(challenge, section);
		return section;
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
		
		for (Entry<Challenge, ConfigurationSection> entry : mExtra.entrySet())
		{
			if (!mCompleted.containsKey(entry.getKey()))
			{
				ConfigurationSection toSave = entry.getValue();
				ConfigurationSection dst = section.createSection(entry.getKey().getName());
				
				for (String key : toSave.getKeys(true))
					dst.set(key, toSave.get(key));
			}
		}
		
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
			Challenge challenge = mIsland.getWorld().getManager().getChallenges().getChallenge(name);
			if (challenge == null)
				continue;
			
			if (section.isConfigurationSection(name))
			{
				mExtra.put(challenge, section.getConfigurationSection(name));
			}
			else
			{
				long date = section.getLong(name, System.currentTimeMillis());
				mCompleted.put(challenge, date);
			}
		}
	}
}
