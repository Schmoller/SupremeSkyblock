package au.com.addstar.skyblock.challenge;

import org.bukkit.entity.Player;

public interface ProgressionChallenge
{
	public float getProgress(ChallengeStorage storage);
	
	public float attemptComplete(Player player, ChallengeStorage storage) throws IllegalStateException;
}
