package au.com.addstar.skyblock.island;

import java.util.UUID;

public class Island
{
	private UUID mOwner;
	private Coord mCoord;
	
	public Island(UUID owner, Coord coords)
	{
		mOwner = owner;
		mCoord = coords;
	}
	
	public UUID getOwner()
	{
		return mOwner;
	}
	
	public Coord getCoord()
	{
		return mCoord;
	}
}
