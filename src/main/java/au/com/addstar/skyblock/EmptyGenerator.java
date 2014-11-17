package au.com.addstar.skyblock;

import java.util.Random;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

public class EmptyGenerator extends ChunkGenerator
{
	@Override
	public byte[][] generateBlockSections( World world, Random random, int x, int z, BiomeGrid biomes )
	{
		// Full empty chunk
		return new byte[16][];
	}
}
