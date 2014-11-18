package au.com.addstar.skyblock.misc;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class Utilities
{
	@SuppressWarnings( "deprecation" )
	public static OfflinePlayer getPlayer(String nameOrUUID)
	{
		try
		{
			UUID id = UUID.fromString(nameOrUUID);
			return Bukkit.getOfflinePlayer(id);
		}
		catch(IllegalArgumentException e)
		{
			OfflinePlayer result = Bukkit.getOfflinePlayer(nameOrUUID);
			if (!result.hasPlayedBefore())
				return null;
			return result;
		}
	}
}
