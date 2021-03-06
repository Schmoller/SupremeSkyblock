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
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;

import com.google.common.collect.Maps;

import au.com.addstar.skyblock.SkyblockWorld;
import au.com.addstar.skyblock.challenge.ChallengeStorage;
import au.com.addstar.skyblock.misc.Utilities;

public class Island
{
	private UUID mOwner;
	private String mOwnerName;
	
	private Map<UUID, String> mMembers;
	private Map<UUID, PlayerSettings> mPlayerSettings;
	
	private final Coord mCoord;
	private final SkyblockWorld mWorld;
	private final BlockVector mIslandOrigin;
	private Location mIslandSpawn;
	private ChallengeStorage mChallenges;
	private int mScore;
	private boolean mAllowWarps;
	
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
		
		mIslandOrigin = new BlockVector(chunkMin.getX() * 16 + halfSize, mWorld.getIslandHeight(), chunkMin.getZ() * 16 + halfSize);
		
		mMembers = Maps.newHashMap();
		for(UUID id : members)
			mMembers.put(id, null);
		
		mPlayerSettings = Maps.newHashMap();
		
		mChallenges = new ChallengeStorage(this);
		
		mIslandStartTime = mLastUseTime = System.currentTimeMillis();
		mAllowWarps = true;
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
		loadIfNeeded();
		
		mOwnerName = ChatColor.stripColor(name);
		mIsModified = true;
	}
	
	public void setOwner(Player player)
	{
		loadIfNeeded();
		
		UUID oldOwner = mOwner;
		mOwner = player.getUniqueId();
		mOwnerName = ChatColor.stripColor(player.getDisplayName());
		mMembers.remove(player.getUniqueId());
		mWorld.updateOwner(this, oldOwner);
		mIsModified = true;
	}
	
	public void setOwnerByMember(UUID member)
	{
		loadIfNeeded();
		
		String name = mMembers.get(member);
		Validate.notNull(name, "That id does not refer to a member");
		UUID oldOwner = mOwner;
		mOwner = member;
		mOwnerName = name;
		mMembers.remove(member);
		mWorld.updateOwner(this, oldOwner);
		mIsModified = true;
	}
	
	public void clear()
	{
		for (Environment environment : Environment.values())
		{
			if (mWorld.getWorld(environment) != null)
				clear(environment);
		}
	}
	
	public void clear(Environment environment)
	{
		Coord min = getChunkCoord();
		for (int x = min.getX(); x < min.getX() + mWorld.getIslandChunkSize(); ++x)
		{
			for (int z = min.getZ(); z < min.getZ() + mWorld.getIslandChunkSize(); ++z)
			{
				World world = mWorld.getWorld(environment);
				Chunk chunk = world.getChunkAt(x, z);
				for (Entity ent : chunk.getEntities())
				{
					if (!(ent instanceof HumanEntity))
						ent.remove();
				}
				// FIXME: Entities do not visually remove
				
				world.regenerateChunk(x, z);
				// Entities do not function after regenerating the chunk until it has been reloaded
				world.unloadChunk(x, z, true, false);
				world.loadChunk(x, z);
			}
		}
		
		mChallenges = new ChallengeStorage(this);
	}
	
	public void placeIsland()
	{
		for (Environment environment : Environment.values())
		{
			if (mWorld.getWorld(environment) != null)
				placeIsland(environment);
		}
	}
	
	public void placeIsland(Environment environment)
	{
		IslandTemplate template;
		if (mOwner.equals(Utilities.spawn))
			template = mWorld.getManager().getSpawnTemplate();
		else
			template = mWorld.getManager().getTemplate(environment);
		
		template.placeAt(getIslandOrigin(environment));
	}
	
	public boolean canAssist(Player player)
	{
		loadIfNeeded();
		
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
	
	public Location getIslandOrigin(Environment environment)
	{
		return mIslandOrigin.toLocation(mWorld.getWorld(environment));
	}
	
	public Location getIslandSpawn()
	{
		loadIfNeeded();
		
		return mIslandSpawn.clone();
	}
	
	public void setIslandSpawn(Location spawn)
	{
		loadIfNeeded();
		
		Validate.isTrue(spawn.getWorld().equals(mWorld.getWorld(spawn.getWorld().getEnvironment())));
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
		loadIfNeeded();
		
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
		loadIfNeeded();
		
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
		loadIfNeeded();
		
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
		loadIfNeeded();
		
		if (mMembers.containsKey(player.getUniqueId()))
		{
			mMembers.remove(player.getUniqueId());
			mPlayerSettings.remove(player.getUniqueId());
			mWorld.updateIslandMembership(this, player.getUniqueId());
			mIsModified = true;
			return true;
		}
		return false;
	}
	
	public Set<UUID> getMembers()
	{
		loadIfNeeded();
		
		return Collections.unmodifiableSet(mMembers.keySet());
	}
	
	public String getMemberName(UUID member)
	{
		loadIfNeeded();
		
		return mMembers.get(member);
	}
	
	public void setMemberName(UUID member, String name)
	{
		loadIfNeeded();
		
		if (mMembers.containsKey(member))
		{
			mMembers.put(member, ChatColor.stripColor(name));
			mIsModified = true;
		}
	}
	
	public PlayerSettings getSettings(UUID player)
	{
		loadIfNeeded();
		
		if (!mPlayerSettings.containsKey(player))
			return new PlayerSettings(player, false);
		else
			return mPlayerSettings.get(player);
	}
	
	public void abandonIsland()
	{
		mWorld.getManager().getPlugin().getLogger().info(String.format("Abandoning island %s (%s)", mCoord, mOwnerName));
		UUID oldOwner = mOwner;
		mOwner = Utilities.nobody;
		mOwnerName = "Unowned";
		mMembers.clear();
		mPlayerSettings.clear();
		
		mWorld.updateOwner(this, oldOwner);
		
		mWorld.getManager().queueAbandoned(this);
		mIsModified = true;
	}
	
	public boolean getWarpAllowed()
	{
		loadIfNeeded();
		return mAllowWarps;
	}
	
	public void setWarpAllowed(boolean value)
	{
		loadIfNeeded();
		mAllowWarps = value;
		mIsModified = true;
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
			
			config.save(new File(base, String.format("%d,%d", mCoord.getX(), mCoord.getZ())));
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
			spawn.set("env", mIslandSpawn.getWorld().getEnvironment().name());
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
		dest.set("allow-warp", mAllowWarps);
		
		ConfigurationSection members = dest.createSection("members");
		for (Entry<UUID, String> member : mMembers.entrySet())
			members.set(member.getKey().toString(), member.getValue());
		
		ConfigurationSection memberSettings = dest.createSection("member-settings");
		for (PlayerSettings settings : mPlayerSettings.values())
			settings.save(memberSettings.createSection(settings.mPlayer.toString()));
		
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
		File islandFile = new File(base, String.format("%d,%d", mCoord.getX(), mCoord.getZ()));
		
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
			Environment environment = Environment.NORMAL;
			if (spawn.contains("env"))
				environment = Environment.valueOf(spawn.getString("env"));
			
			mIslandSpawn = new Location(mWorld.getWorld(environment), spawn.getDouble("x"), spawn.getDouble("y"), spawn.getDouble("z"), (float)spawn.getDouble("yaw"), (float)spawn.getDouble("pitch"));
		}
		
		if (source.contains("owner-name"))
			mOwnerName = source.getString("owner-name");
		
		mScore = source.getInt("score", 0);
		mIslandStartTime = source.getLong("start-time", System.currentTimeMillis());
		mLastUseTime = source.getLong("use-time", System.currentTimeMillis());
		mAllowWarps = source.getBoolean("allow-warp", true);
		
		if (source.isConfigurationSection("members"))
		{
			ConfigurationSection members = source.getConfigurationSection("members");
			for (String key : members.getKeys(false))
			{
				UUID id = UUID.fromString(key);
				mMembers.put(id, members.getString(key));
			}
		}
		
		if (source.isConfigurationSection("member-settings"))
		{
			ConfigurationSection memberSettings = source.getConfigurationSection("member-settings");
			for (String key : memberSettings.getKeys(false))
			{
				UUID id = UUID.fromString(key);
				PlayerSettings settings = new PlayerSettings(id, true);
				settings.load(memberSettings.getConfigurationSection(key));
				mPlayerSettings.put(id, settings);
			}
		}
		
		mChallenges.load(source);
	}
	
	public class PlayerSettings
	{
		private final UUID mPlayer;
		private Location mHome;
		private boolean mAdded;
		
		protected PlayerSettings(UUID player, boolean added)
		{
			mPlayer = player;
			mAdded = added;
		}
		
		private void addIfNeeded()
		{
			if (!mAdded)
			{
				mPlayerSettings.put(mPlayer, this);
				mAdded = true;
			}
		}
		
		public UUID getPlayer()
		{
			return mPlayer;
		}
		
		public Location getHome()
		{
			return mHome;
		}
		
		public void setHome(Location location)
		{
			mHome = location;
			addIfNeeded();
			mIsModified = true;
		}
		
		protected void load(ConfigurationSection section)
		{
			if (section.isConfigurationSection("home"))
			{
				ConfigurationSection sub = section.getConfigurationSection("home");
				Environment environment = Environment.valueOf(sub.getString("env", "NORMAL"));
				mHome = new Location(mWorld.getWorld(environment), sub.getDouble("x", 0), sub.getDouble("y", 0), sub.getDouble("z", 0), (float)sub.getDouble("yaw", 0), (float)sub.getDouble("pitch", 0));
			}
		}
		
		protected void save(ConfigurationSection section)
		{
			if (mHome != null)
			{
				ConfigurationSection sub = section.createSection("home");
				sub.set("env", mHome.getWorld().getEnvironment().name());
				sub.set("x", mHome.getX());
				sub.set("y", mHome.getY());
				sub.set("z", mHome.getZ());
				sub.set("yaw", mHome.getYaw());
				sub.set("pitch", mHome.getPitch());
			}
		}
	}
}
