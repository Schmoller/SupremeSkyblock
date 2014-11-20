package au.com.addstar.skyblock.challenge;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import au.com.addstar.skyblock.SkyblockManager;

import com.google.common.collect.HashMultimap;

public class ChallengeManager
{
	private HashMap<String, Challenge> mLoadedChallenges;
	private SkyblockManager mManager;
	
	public ChallengeManager(SkyblockManager manager)
	{
		mLoadedChallenges = new HashMap<String, Challenge>();
		mManager = manager;
	}
	
	public Challenge getChallenge(String name)
	{
		return mLoadedChallenges.get(name);
	}
	
	public void addChallenge(Challenge challenge)
	{
		Validate.isTrue(!mLoadedChallenges.containsKey(challenge.getName()), "Duplicate name");
		
		mLoadedChallenges.put(challenge.getName(), challenge);
	}
	
	public Collection<Challenge> getChallenges()
	{
		return Collections.unmodifiableCollection(mLoadedChallenges.values());
	}
	
	public void loadChallenges(File file)
	{
		if (!file.exists())
			return;
		
		mManager.getPlugin().getLogger().info("Loading challenges");
		HashMultimap<Challenge, String> toMap = HashMultimap.create();
		
		try
		{
			YamlConfiguration config = new YamlConfiguration();
			config.load(file);
			
			for (String name : config.getKeys(false))
			{
				if (!config.isConfigurationSection(name))
				{
					mManager.getPlugin().getLogger().severe("Challenge " + name + " is not a valid entry");
					continue;
				}
				
				ConfigurationSection section = config.getConfigurationSection(name);
				
				try
				{
					String typeName = section.getString("type");
					if (typeName == null)
						throw new IllegalArgumentException("Challenge type is not defined. Expected 'type: <type>'");
					
					Challenge challenge = null;
					
					for(ChallengeType type : ChallengeType.values())
					{
						if (type.getConfigName().equalsIgnoreCase(typeName))
						{
							challenge = type.createChallenge(name);
							break;
						}
					}
					
					if (challenge == null)
						throw new IllegalArgumentException("Unknown challenge type " + typeName);
					
					challenge.load(section);
					
					if (section.isList("depends"))
					{
						for(String depend : section.getStringList("depends"))
							toMap.put(challenge, depend);
					}
					
					addChallenge(challenge);
				}
				catch(IllegalArgumentException e)
				{
					mManager.getPlugin().getLogger().severe("Error reading challenge " + name + ": " + e.getMessage());
				}
			}
			
			// Link up the challenges
			for (Challenge challenge : toMap.keySet())
			{
				for (String name : toMap.get(challenge))
				{
					Challenge depend = getChallenge(name);
					if (depend == null)
						mManager.getPlugin().getLogger().warning("Error reading challenge " + challenge.getName() + ": Unknown dependency " + name);
					else
						challenge.getDependencies().add(depend);
				}
			}
			
			mManager.getPlugin().getLogger().info("Loaded " + mLoadedChallenges.size() + " challenges");
		}
		catch(IOException e)
		{
			mManager.getPlugin().getLogger().log(Level.SEVERE, "Error reading challenges file", e);
		}
		catch(InvalidConfigurationException e)
		{
			mManager.getPlugin().getLogger().log(Level.SEVERE, "Error reading challenges file", e);
		}
	}
}