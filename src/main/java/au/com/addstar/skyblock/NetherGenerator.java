package au.com.addstar.skyblock;

import java.util.Random;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;

@SuppressWarnings( "deprecation" )
public class NetherGenerator extends ChunkGenerator
{
	@Override
	public byte[][] generateBlockSections( World world, Random random, int x, int z, BiomeGrid biomes )
	{
		byte[][] sections = new byte[16][];
		
		// First section will be populated mostly with lava
		sections[0] = new byte[4096];
		
		for (int xx = 0; xx < 16; ++xx)
		{
			for (int zz = 0; zz < 16; ++zz)
			{
				biomes.setBiome(xx, zz, Biome.HELL);
				
				for (int yy = 0; yy < 12; ++yy)
					sections[0][yy << 8 | (zz << 4) | xx] = (byte)Material.STATIONARY_LAVA.getId();
			}
		}
		
		return sections;
	}
}
