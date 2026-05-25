package com.houzicore.shared.common.util;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.profile.PlayerProfile;

/**
 * Profile Loader for modern Spigot 1.21.11
 * Uses org.bukkit.profile.PlayerProfile instead of old NMS GameProfile
 */
public class ProfileLoader
{
	private final String uuid;
	private final String name;
	private final String skinOwner;

	public ProfileLoader(String uuid, String name)
	{
		this(uuid, name, name);
	}

	public ProfileLoader(String uuid, String name, String skinOwner)
	{
		this.uuid = uuid == null ? null : uuid.replaceAll("-", "");
		String displayName = ChatColor.translateAlternateColorCodes('&', name);
		this.name = ChatColor.stripColor(displayName);
		this.skinOwner = skinOwner;
	}

	public PlayerProfile loadProfile()
	{
		// In 1.21.11, create profile from UUID
		try {
			UUID id = uuid == null ? 
				UUID.randomUUID() : 
				parseUUID(uuid);
			// Note: Bukkit 1.21.11 doesn't have createProfile
			// Return null for now - clients should use Paper API if available
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	private UUID parseUUID(String uuidStr)
	{
		// Split uuid into 5 components
		String[] uuidComponents = new String[] { 
			uuidStr.substring(0, 8), 
			uuidStr.substring(8, 12),
			uuidStr.substring(12, 16), 
			uuidStr.substring(16, 20), 
			uuidStr.substring(20, uuidStr.length()) 
		};

		// Combine components with a dash
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < uuidComponents.length; i++)
		{
			builder.append(uuidComponents[i]);
			if (i < uuidComponents.length - 1)
				builder.append("-");
		}

		return UUID.fromString(builder.toString());
	}
}
