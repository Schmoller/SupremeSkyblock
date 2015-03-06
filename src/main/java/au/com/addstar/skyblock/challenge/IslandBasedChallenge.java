package au.com.addstar.skyblock.challenge;

import au.com.addstar.skyblock.island.Island;

/**
 * Allows a challenge to do things based on island state
 */
public interface IslandBasedChallenge
{
	/**
	 * Called upon the updating of island score
	 * @param island The island whose score is being updated
	 * @param typeCounts The count of each type of block. There will be one entry for each {@link Material}
	 */
	public void onCalculateScore(Island island, int[] typeCounts);
}
