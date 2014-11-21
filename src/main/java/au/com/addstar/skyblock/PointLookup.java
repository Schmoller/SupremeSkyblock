package au.com.addstar.skyblock;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import au.com.addstar.monolith.lookup.Lookup;

public class PointLookup
{
	private int mDefaultScore = 1;
	private int mDefaultLimit = -1;
	private int mPointsPerLevel = 100;
	
	private HashMap<Material, Integer> mScores;
	private HashMap<Material, Integer> mLimits;
	
	public PointLookup()
	{
		mScores = new HashMap<Material, Integer>();
		mLimits = new HashMap<Material, Integer>();
	}
	
	public int getScore(Block block)
	{
		return getScore(block.getType());
	}
	
	public int getScore(Material material)
	{
		if (mScores.containsKey(material))
			return mScores.get(material);
		return mDefaultScore;
	}
	
	public int getLimit(Block block)
	{
		return getLimit(block.getType());
	}
	
	public int getLimit(Material material)
	{
		if (mLimits.containsKey(material))
			return mLimits.get(material);
		return mDefaultLimit;
	}
	
	public int getPointsPerLevel()
	{
		return mPointsPerLevel;
	}
	
	public void load(File file, Logger logger)
	{
		mDefaultScore = 1;
		mDefaultLimit = -1;
		
		mScores.clear();
		mLimits.clear();
		
		if (!file.exists())
			return;
		
		try
		{
			YamlConfiguration config = new YamlConfiguration();
			config.load(file);
			
			if (config.isConfigurationSection("rank"))
			{
				ConfigurationSection section = config.getConfigurationSection("rank");
				mPointsPerLevel = section.getInt("points-per-level", 100);
			}
			
			if (config.isConfigurationSection("block-value"))
			{
				ConfigurationSection section = config.getConfigurationSection("block-value");
				mDefaultScore = section.getInt("default", 1);
				for(String name : section.getKeys(false))
				{
					if (name.equals("default") || !section.isInt(name))
						continue;
					
					Material mat = Lookup.findByMinecraftName(name);
					if (mat == null)
					{
						mat = Material.getMaterial(name.toUpperCase());
						
						if (mat == null)
						{
							logger.warning("Unknown material name " + name + " when loading block values in " + file.getName());
							continue;
						}
					}
					
					mScores.put(mat, section.getInt(name));
				}
			}
			
			if (config.isConfigurationSection("block-limits"))
			{
				ConfigurationSection section = config.getConfigurationSection("block-limits");
				mDefaultLimit = section.getInt("default", -1);
				for(String name : section.getKeys(false))
				{
					if (name.equals("default") || !section.isInt(name))
						continue;
					
					Material mat = Lookup.findByMinecraftName(name);
					if (mat == null)
					{
						mat = Material.getMaterial(name.toUpperCase());
						
						if (mat == null)
						{
							logger.warning("Unknown material name " + name + " when loading block limits in " + file.getName());
							continue;
						}
					}
					
					mLimits.put(mat, section.getInt(name));
				}
			}
		}
		catch(InvalidConfigurationException e)
		{
			logger.log(Level.SEVERE, "Failed to load " + file.getName() + "! There is an error in the file:", e);
		}
		catch(IOException e)
		{
			logger.log(Level.SEVERE, "Failed to load " + file.getName() + "! There was an error loading it:", e);
		}
	}
}
