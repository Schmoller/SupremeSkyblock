package au.com.addstar.skyblock.island;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.material.MaterialData;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.Region;

import au.com.addstar.skyblock.misc.StoredBlock;

public class IslandTemplate
{
	private static final String fileHeader = "SCHEM";
	
	private StoredBlock[] mBlocks;
	
	private int mWidth;
	private int mHeight;
	private int mDepth;
	
	private Location mSpawn;
	
	private boolean checkType( DataInputStream in ) throws IOException
	{
		for (int i = 0; i < fileHeader.length(); ++i)
		{
			if (in.readChar() != fileHeader.charAt(i))
				return false;
		}

		return true;
	}
	
	public boolean load(Region region, Location spawn)
	{
		mWidth = region.getWidth();
		mHeight = region.getHeight();
		mDepth = region.getLength();
		
		mBlocks = new StoredBlock[mWidth * mHeight * mDepth];
		
		Arrays.fill(mBlocks, new StoredBlock(Material.AIR));
		World world = BukkitUtil.toWorld(region.getWorld());
		Vector min = region.getMinimumPoint();
		
		for(BlockVector vec : region)
		{
			Block block = world.getBlockAt(vec.getBlockX(), vec.getBlockY(), vec.getBlockZ());
			int x = vec.getBlockX() - min.getBlockX();
			int y = vec.getBlockY() - min.getBlockY();
			int z = vec.getBlockZ() - min.getBlockZ();
			
			mBlocks[x + y * mWidth + z * (mWidth * mHeight)] = new StoredBlock(block.getState());
		}
		
		mSpawn = new Location(null, spawn.getX() - min.getBlockX(), spawn.getY() - min.getBlockY(), spawn.getZ() - min.getBlockZ(), spawn.getYaw(), spawn.getPitch());
		
		return true;
	}
	
	public boolean load(File file)
	{
		DataInputStream in = null;
		try
		{
			FileInputStream stream = new FileInputStream(file);
			in = new DataInputStream(stream);

			return load(in);
		}
		catch ( IOException e )
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			if ( in != null )
			{
				try
				{
					in.close();
				}
				catch ( IOException e )
				{
				}
			}
		}
	}
	
	public boolean load(InputStream stream)
	{
		DataInputStream in = new DataInputStream(stream);
		try
		{
			return load(in);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	@SuppressWarnings( "deprecation" )
	public boolean load(DataInputStream in) throws IOException
	{
		if (!checkType(in))
			return false;

		int version = in.readUnsignedByte();
		if (version != 1)
			return false;

		mWidth = in.readUnsignedShort();
		mHeight = in.readUnsignedShort();
		mDepth = in.readUnsignedShort();
		
		// Read the spawn location
		mSpawn = new Location(null, in.readDouble(), in.readDouble(), in.readDouble(), in.readFloat(), in.readFloat());

		// Read the materials
		HashMap<Integer, MaterialData> mats = new HashMap<Integer, MaterialData>();
		int matCount = in.readShort();
		for (int i = 0; i < matCount; ++i)
		{
			int id = in.readUnsignedShort();
			String name = in.readUTF();
			byte data = in.readByte();

			mats.put(id, new MaterialData(Material.valueOf(name), data));
		}
		
		// Read blocks
		mBlocks = new StoredBlock[mWidth * mHeight * mDepth];
		for (int i = 0; i < mBlocks.length; ++i)
		{
			StoredBlock block = new StoredBlock();
			block.read(in, mats);
			mBlocks[i] = block;
		}
		
		return true;
	}
	
	@SuppressWarnings( "deprecation" )
	public boolean save(File file)
	{
		HashMap<MaterialData, Integer> ids = new HashMap<MaterialData, Integer>();
		int nextId = 0;
		
		int[] blockIds = new int[mBlocks.length];
		
		for (int i = 0; i < mBlocks.length; ++i)
		{
			StoredBlock block = mBlocks[i];
			
			int id;
			if (!ids.containsKey(block.getType()))
			{
				id = nextId++;
				ids.put(block.getType(), id);
			}
			else
				id = ids.get(block.getType());
			
			blockIds[i] = id;
		}

		DataOutputStream out = null;
		try
		{
			FileOutputStream stream = new FileOutputStream(file);
			out = new DataOutputStream(stream);

			out.writeChars(fileHeader);
			out.writeByte(1); // Version

			out.writeShort(mWidth);
			out.writeShort(mHeight);
			out.writeShort(mDepth);
			
			// Write spawn
			out.writeDouble(mSpawn.getX());
			out.writeDouble(mSpawn.getY());
			out.writeDouble(mSpawn.getZ());
			out.writeFloat(mSpawn.getYaw());
			out.writeFloat(mSpawn.getPitch());

			// Write material map
			out.writeShort(ids.size());
			for (Entry<MaterialData, Integer> entry : ids.entrySet())
			{
				out.writeShort(entry.getValue());
				out.writeUTF(entry.getKey().getItemType().name());
				out.writeByte(entry.getKey().getData());
			}

			// Write blocks
			for (int i = 0; i < mBlocks.length; ++i)
				mBlocks[i].write(out, blockIds[i]);
			return true;
		}
		catch ( IOException e )
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			if ( out != null )
			{
				try
				{
					out.close();
				}
				catch ( IOException e )
				{
				}
			}
		}
	}
}
