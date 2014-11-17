package au.com.addstar.skyblock.island;

public class Coord
{
	private int mX;
	private int mZ;
	
	public Coord(int x, int z)
	{
		mX = x;
		mZ = z;
	}
	
	public int getX()
	{
		return mX;
	}
	
	public int getZ()
	{
		return mZ;
	}
	
	public void setX(int x)
	{
		mX = x;
	}
	
	public void setZ(int z)
	{
		mZ = z;
	}
	
	@Override
	public int hashCode()
	{
		return ((mX & 0xFFFF) << 16) | (mZ & 0xFFFF); 
	}
	
	@Override
	public boolean equals( Object obj )
	{
		if (!(obj instanceof Coord))
			return false;
		
		return ((Coord)obj).mX == mX && ((Coord)obj).mZ == mZ;
	}
	
	@Override
	public String toString()
	{
		return String.format("%d,%d", mX, mZ);
	}
}
