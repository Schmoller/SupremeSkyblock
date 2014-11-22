package au.com.addstar.skyblock.island;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import com.google.common.collect.Maps;

import au.com.addstar.skyblock.SkyblockWorld;
import au.com.addstar.skyblock.challenge.ChallengeStorage;

public class Island
{
	private UUID mOwner;
	private String mOwnerName;
	
	private Map<UUID, String> mMembers;
	
	private final Coord mCoord;
	private final SkyblockWorld mWorld;
	private final Location mIslandOrigin;
	private Location mIslandSpawn;
	private ChallengeStorage mChallenges;
	private int mScore;
	
	private long mIslandStartTime;
	private long mLastUseTime;
	
	private boolean mHasLoaded = false;
	private boolean mIsModified = false;
	private boolean mScoreDirty = false;
	
	public Island(UUID owner, List<UUID> members, Coord coords, SkyblockWorld world)
	{
		mOwner = owner;
		mCoord = coords;
		mWorld = world;

		Coord chunkMin = getChunkCoord();
		int halfSize = (mWorld.getIslandChunkSize() * 16) / 2;
		
		mIslandOrigin = new Location(mWorld.getWorld(), chunkMin.getX() * 16 + halfSize, 190, chunkMin.getZ() * 16 + halfSize);
		
		mMembers = Maps.newHashMap();
		for(UUID id : members)
			mMembers.put(id, null);
		
		mChallenges = new ChallengeStorage(this);
		
		mIslandStartTime = mLastUseTime = System.currentTimeMillis();
	}
	
	public UUID getOwner()
	{
		return mOwner;
	}
	
	public String getOwnerName()
	{
		loadIfNeeded();
		
		return mOwnerName;
	}
	
	public void setOwnerName(String name)
	{
		mOwnerName = name;
		mIsModified = true;
	}
	
	public void clear()
	{
		Coord min = getChunkCoord();
		for (int x = min.getX(); x < min.getX() + mWorld.getIslandChunkSize(); ++x)
		{
			for (int z = min.getZ(); z < min.getZ() + mWorld.getIslandChunkSize(); ++z)
			{
				Chunk chunk = mWorld.getWorld().getChunkAt(x, z);
				for (Entity ent : chunk.getEntities())
				{
					if (!(ent instanceof HumanEntity))
						ent.remove();
				}
				// FIXME: Entities do not visually remove
				
				mWorld.getWorld().regenerateChunk(x, z);
				
				// FIXME: Empty chunks are not being sent correctly to the client. They appear as missing chunks causing several client issues
			}
		}
		
		mChallenges = new ChallengeStorage(this);
	}
	
	public void placeIsland()
	{
		mWorld.getManager().getTemplate().placeAt(mIslandOrigin);
	}
	
	public boolean canAssist(Player player)
	{
		if (player.getUniqueId().equals(mOwner))
			return true;
		
		return mMembers.containsKey(player.getUniqueId());
	}
	
	public Coord getCoord()
	{
		return mCoord;
	}
	
	public SkyblockWorld getWorld()
	{
		return mWorld;
	}
	
	public Coord getChunkCoord()
	{
		return new Coord(mCoord.getX() * mWorld.getIslandChunkSize(), mCoord.getZ() * mWorld.getIslandChunkSize());
	}
	
	public Location getIslandOrigin()
	{
		return mIslandOrigin.clone();
	}
	
	public Location getIslandSpawn()
	{
		loadIfNeeded();
		
		return mIslandSpawn.clone();
	}
	
	public void setIslandSpawn(Location spawn)
	{
		Validate.isTrue(spawn.getWorld().equals(mIslandOrigin.getWorld()));
		mIslandSpawn = spawn;
		
		mIsModified = true;
	}
	
	public int getRank()
	{
		return mWorld.getManager().getRank(this);
	}
	
	public int getScore()
	{
		loadIfNeeded();
		return mScore;
	}
	
	public void setScore(int score)
	{
		mScore = score;
		
		mWorld.getManager().updateRank(this);
		mScoreDirty = false;
		mIsModified = true;
	}
	
	public boolean isScoreDirty()
	{
		return mScoreDirty;
	}
	
	public void markScoreDirty()
	{
		if (!mScoreDirty)
		{
			mScoreDirty = true;
			mWorld.getManager().queueScoreUpdate(this);
		}
	}
	
	public long getStartTime()
	{
		loadIfNeeded();
		return mIslandStartTime;
	}
	
	public long getLastUseTime()
	{
		loadIfNeeded();
		return mLastUseTime;
	}
	
	public void setLastUseTime(long time)
	{
		mLastUseTime = time;
		mIsModified = true;
	}
	
	public ChallengeStorage getChallengeStorage()
	{
		loadIfNeeded();
		return mChallenges;
	}
	
	public void addMember(OfflinePlayer player)
	{
		String name;
		if (player.isOnline())
			name = ChatColor.stripColor(player.getPlayer().getDisplayName());
		else
			name = player.getName();
		
		mMembers.put(player.getUniqueId(), name);
		mWorld.updateIslandMembership(this, player.getUniqueId());
		mIsModified = true;
	}
	
	public boolean removeMember(OfflinePlayer player)
	{
		if (mMembers.containsKey(player.getUniqueId()))
		{
			mMembers.remove(player.getUniqueId());
			mWorld.updateIslandMembership(this, player.getUniqueId());
			mIsModified = true;
			return true;
		}
		return false;
	}
	
	public Set<UUID> getMembers()
	{
		return Collections.unmodifiableSet(mMembers.keySet());
	}
	
	public String getMemberName(UUID member)
	{
		return mMembers.get(member);
	}
	
	public void saveIfNeeded()
	{
		if (mIsModified || mChallenges.needsSaving())
			save();
	}
	
	public void save()
	{
		try
		{
			YamlConfiguration config = new YamlConfiguration();
			config.options().header("WARNING! This file is automatically generated. Do not edit this file");
			
			File base = new File(mWorld.getManager().getWorldFolder(mWorld.getName()), "islands");
			if (!base.exists())
				base.mkdirs();
			
			save(config);
			
			config.save(new File(base, mOwner.toString()));
		}
		catch(IOException e)
		{
			mWorld.getManager().getPlugin().getLogger().warning("Failed to save island for " + mOwner);
			e.printStackTrace();
		}
		
		mHasLoaded = true;
		mIsModified = false;
	}
	
	private void save(ConfigurationSection dest)
	{
		if (mIslandSpawn != null)
		{
			ConfigurationSection spawn = dest.createSection("spawn");
			spawn.set("x", mIslandSpawn.getX());
			spawn.set("y", mIslandSpawn.getY());
			spawn.set("z", mIslandSpawn.getZ());
			spawn.set("yaw", mIslandSpawn.getYaw());
			spawn.set("pitch", mIslandSpawn.getPitch());
		}
		
		if (mOwnerName != null)
			dest.set("owner-name", mOwnerName);
		
		dest.set("score", mScore);
		dest.set("start-time", mIslandStartTime);
		dest.set("use-date", mLastUseTime);
		
		ConfigurationSection members = dest.createSection("members");
		for (Entry<UUID, String> member : mMembers.entrySet())
			members.set(member.getKey().toString(), member.getValue());
		
		mChallenges.save(dest);
	}
	
	public void loadIfNeeded()
	{
		if (!mHasLoaded)
			load();
	}
	
	public void load()
	{
		File base = new File(mWorld.getManager().getWorldFolder(mWorld.getName()), "islands");
		File islandFile = new File(base, mOwner.toString());
		
		if (!islandFile.exists())
			return;
		
		try
		{
			YamlConfiguration config = new YamlConfiguration();
			config.load(islandFile);
			
			load(config);
		}
		catch(IOException e)
		{
			mWorld.getManager().getPlugin().getLogger().warning("Failed to load island for " + mOwner);
			e.printStackTrace();
		}
		catch(InvalidConfigurationException e)
		{
			mWorld.getManager().getPlugin().getLogger().warning("Failed to load island for " + mOwner);
			e.printStackTrace();
		}
		
		mHasLoaded = true;
		mIsModified = false;
		mScoreDirty = false;
	}
	
	private void load(ConfigurationSection source)
	{
		if (source.isConfigurationSection("spawn"))
		{
			ConfigurationSection spawn = source.getConfigurationSection("spawn");
			mIslandSpawn = new Location(mWorld.getWorld(), spawn.getDouble("x"), spawn.getDouble("y"), spawn.getDouble("z"), (float)spawn.getDouble("yaw"), (float)spawn.getDouble("pitch"));
		}
		
		if (source.contains("owner-name"))
			mOwnerName = source.getString("owner-name");
		
		mScore = source.getInt("score", 0);
		mIslandStartTime = source.getLong("start-time", System.currentTimeMillis());
		mLastUseTime = source.getLong("use-time", System.currentTimeMillis());
		
		if (source.isConfigurationSection("members"))
		{
			ConfigurationSection members = source.getConfigurationSection("members");
			for (String key : members.getKeys(false))
			{
				UUID id = UUID.fromString(key);
				mMembers.put(id, members.getString(key));
			}
		}
		
		mChallenges.load(source);
	}
}
