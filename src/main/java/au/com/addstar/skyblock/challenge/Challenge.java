package au.com.addstar.skyblock.challenge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import au.com.addstar.skyblock.challenge.rewards.Reward;
import au.com.addstar.skyblock.island.Island;
import au.com.addstar.skyblock.misc.Utilities;

public abstract class Challenge
{
	private final String mName;
	private List<String> mDescription;
	private boolean mShowRequirements;
	
	private List<Reward> mPrimaryRewards;
	private List<Reward> mSecondaryRewards;
	private int mPoints;
	
	private boolean mCanRepeat;
	private long mCooldown;
	
	private List<Challenge> mRequired;
	
	protected Challenge(String name)
	{
		mName = name;
		
		mPrimaryRewards = new ArrayList<Reward>();
		mSecondaryRewards = new ArrayList<Reward>();
		mRequired = new ArrayList<Challenge>();
		mDescription = Collections.emptyList();
		
		mShowRequirements = true;
	}
	
	/**
	 * Does a final check that the challenge can be completed, then removes from the player or island whatever needs to be removed for the challange
	 * @param player The player completing the challenge
	 * @param island The island the challenge is being completed for
	 * @throws IllegalStateException Thrown if the challenge may not be completed with the reason in the message
	 */
	protected abstract void onComplete(Player player, Island island) throws IllegalStateException;
	
	/**
	 * When true, players cannot call complete on this challenge. Some other method has to be used to make it happen
	 * @return True when players can complete it via command
	 */
	public abstract boolean isManual();
	
	protected abstract void addRequirementDescription(ImmutableList.Builder<String> builder, boolean completed, ChallengeStorage storage );
	
	protected void checkCanComplete(ChallengeStorage storage)
	{
		if (!storage.allComplete(mRequired))
			throw new IllegalStateException("You have not completed the required challenges");
		
		if (!isRepeatable())
		{
			if (storage.isComplete(this))
				throw new IllegalStateException("This challenge has already been completed");
		}
		else
		{
			if (storage.isComplete(this))
			{
				if (System.currentTimeMillis() - storage.getCompletionTime(this) < getCooldownLength())
					throw new IllegalStateException("This challenge is still cooling down. You cannot complete it at this time");
			}
		}
	}
	
	private void assignAwards(Player player, boolean secondary)
	{
		List<Reward> rewards;
		if (secondary)
			rewards = mSecondaryRewards;
		else
			rewards = mPrimaryRewards;
		
		for (Reward reward : rewards)
			reward.apply(player);
	}
	
	public void complete(Player player, ChallengeStorage storage) throws IllegalStateException
	{
		checkCanComplete(storage);
		
		onComplete(player, storage.getIsland());
		
		assignAwards(player, storage.isComplete(this));
		storage.markComplete(this);
	}
	
	protected void addRewardDescription(ImmutableList.Builder<String> builder, boolean secondary)
	{
		List<Reward> rewards;
		if (secondary)
			rewards = mSecondaryRewards;
		else
			rewards = mPrimaryRewards;
		
		builder.add(Utilities.format(" &fReward:"));
		for (Reward reward : rewards)
			builder.add(Utilities.format("&7 - &7%s", reward.getName()));
	}
	
	public final void addDescription(ImmutableList.Builder<String> builder, ChallengeStorage storage)
	{
		boolean completed = storage.isComplete(this);
		for(String string : mDescription)
			builder.add(Utilities.format(" &7&o%s", string));
		
		if (mShowRequirements)
			addRequirementDescription(builder, completed && !isRepeatable(), storage);
		
		if (!completed || isRepeatable())
		{
			addRewardDescription(builder, completed);
			if (this instanceof ProgressionChallenge)
			{
				float progress = ((ProgressionChallenge)this).getProgress(storage);
				if (progress > 0)
					builder.add(Utilities.format(" &7You are %d%% towards completing this challenge", (int)(progress * 100)));
			}
		}
		else
			builder.add(Utilities.format(" &aYou have completed this challenge"));
	}
	
	public void load(ConfigurationSection section)
	{
		if (section.isList("description"))
			mDescription = section.getStringList("description");
		
		mShowRequirements = section.getBoolean("show-reqs", true);
		
		mCanRepeat = section.getBoolean("repeat", false);
		mCooldown = TimeUnit.MILLISECONDS.convert(section.getLong("cooldown", 2), TimeUnit.MINUTES);
		
		if (section.isList("reward"))
			mPrimaryRewards = loadRewards(section.getStringList("reward"));
		
		if (section.isList("secondary-reward"))
			mSecondaryRewards = loadRewards(section.getStringList("secondary-reward"));
		else
			mSecondaryRewards = new ArrayList<Reward>(mPrimaryRewards);
		
		mPoints = section.getInt("points", 0);
	}
	
	private List<Reward> loadRewards(List<String> list)
	{
		ArrayList<Reward> rewards = new ArrayList<Reward>(list.size());
		for(String def : list)
			rewards.add(Reward.load(def));
		
		return rewards;
	}
	
	public boolean isRepeatable()
	{
		return mCanRepeat;
	}
	
	public void setRepeatable(boolean isRepeatable)
	{
		mCanRepeat = isRepeatable;
	}
	
	public long getCooldownLength()
	{
		return mCooldown;
	}
	
	public void setCooldownLength(long length)
	{
		mCooldown = length;
	}
	
	public List<Challenge> getDependencies()
	{
		return mRequired;
	}
	
	public boolean showRequirements()
	{
		return mShowRequirements;
	}
	
	public void setShowRequirements(boolean show)
	{
		mShowRequirements = show;
	}
	
	public String getName()
	{
		return mName;
	}
	
	public List<String> getDescription()
	{
		return mDescription;
	}
	
	public void setDescription(List<String> description)
	{
		mDescription = description;
	}
	
	public List<Reward> getPrimaryRewards()
	{
		return mPrimaryRewards;
	}
	
	public List<Reward> getSecondaryRewards()
	{
		return mSecondaryRewards;
	}
	
	public int getPointReward()
	{
		return mPoints;
	}
	
	public void setPointReward(int points)
	{
		mPoints = points;
	}
}
