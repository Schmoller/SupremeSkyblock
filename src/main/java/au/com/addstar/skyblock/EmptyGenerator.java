package au.com.addstar.skyblock;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

public class EmptyGenerator extends ChunkGenerator
{
	@SuppressWarnings( "deprecation" )
	@Override
	public byte[][] generateBlockSections( World world, Random random, int x, int z, BiomeGrid biomes )
	{
		byte[][] chunk = new byte[16][];
		chunk[0] = new byte[16*16*16];
		// Bukkit cannot send a single empty chunk, it is interpreted as a chunk unload request
		// So we use this invisible block to fix this
		chunk[0][0] = (byte)Material.PISTON_MOVING_PIECE.getId();
		
		return chunk;
	}
}
