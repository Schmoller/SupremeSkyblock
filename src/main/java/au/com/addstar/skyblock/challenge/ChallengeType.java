package au.com.addstar.skyblock.challenge;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import au.com.addstar.skyblock.challenge.types.CraftChallenge;
import au.com.addstar.skyblock.challenge.types.DetectChallenge;
import au.com.addstar.skyblock.challenge.types.SubmitChallenge;

public enum ChallengeType
{
	Submit("submit", SubmitChallenge.class),
	Detect("detect", DetectChallenge.class),
	Craft("craft", CraftChallenge.class);
	
	private String mName;
	private Constructor<? extends Challenge> mNewChallenge;
	
	private ChallengeType(String name, Class<? extends Challenge> challengeClass)
	{
		mName = name;

		try
		{
			mNewChallenge = challengeClass.getConstructor(String.class);
		}
		catch(NoSuchMethodException e)
		{
			throw new ExceptionInInitializerError(e);
		}
	}
	
	public String getConfigName()
	{
		return mName;
	}
	
	public Challenge createChallenge(String name)
	{
		try
		{
			return mNewChallenge.newInstance(name);
		}
		catch(InvocationTargetException e)
		{
			throw new RuntimeException(e.getCause());
		}
		catch(Exception e)
		{
			// Should not happen
			throw new AssertionError(e);
		}
	}
}
