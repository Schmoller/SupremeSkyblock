package au.com.addstar.skyblock.challenge.rewards;

import org.bukkit.entity.Player;

import au.com.addstar.skyblock.misc.Utilities;

public class XPReward extends Reward
{
	private int mXP;
	private boolean mLevel;
	public XPReward(int xp, boolean level)
	{
		mXP = xp;
		mLevel = level;
	}
	
	@Override
	public void apply( Player player )
	{
		if (mLevel)
			player.giveExpLevels(mXP);
		else
			player.giveExp(mXP);
	}
	
	@Override
	public String getName()
	{
		if (mLevel)
			return Utilities.format("&e%d &7levels of xp", mXP);
		else
			return Utilities.format("&e%d &7xp", mXP);
	}
}
