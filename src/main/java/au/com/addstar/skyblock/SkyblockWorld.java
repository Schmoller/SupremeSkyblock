package au.com.addstar.skyblock;

import java.util.Random;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;

public class SkyblockWorld
{
	private static Random mRand = new Random();
	
	private String mName;
	private World mWorld;
	
	public SkyblockWorld(String name, ConfigurationSection config)
	{
		long seed = config.getLong("seed", mRand.nextLong());
		
		mName = name;
		mWorld = new WorldCreator(name)
		.environment(Environment.NORMAL)
		.generateStructures(false)
		.generator(new EmptyGenerator())
		.type(WorldType.FLAT)
		.seed(seed)
		.createWorld();
	}
	
	public String getName()
	{
		return mName;
	}
	
	public World getWorld()
	{
		return mWorld;
	}
	
	public void save(ConfigurationSection config)
	{
		config.set("seed", mWorld.getSeed());
	}
}
