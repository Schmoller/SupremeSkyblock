package au.com.addstar.skyblock.challenge;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import org.apache.commons.lang.Validate;

public class ChallengeManager
{
	private HashMap<String, Challenge> mLoadedChallenges;
	
	public ChallengeManager()
	{
		mLoadedChallenges = new HashMap<String, Challenge>();
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
		throw new UnsupportedOperationException("Not yet implemented");
	}
}