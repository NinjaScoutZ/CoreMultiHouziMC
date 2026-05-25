package com.houzicore.shared.common.util;

import com.houzicore.shared.common.actionbar.ActionBarChannel;
import com.houzicore.shared.common.actionbar.ActionBarService;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Action Bar text utility - migrated to Adventure API for Paper 1.21.1
 */
public class UtilTextBottom
{
	public static void display(String text, Player... players)
	{
		display(ActionBarChannel.LEGACY, text, players);
	}

	public static void display(ActionBarChannel channel, String text, Player... players)
	{
		channel = channel == null ? ActionBarChannel.LEGACY : channel;
		display(channel, text, channel.getDefaultTtlMs(), players);
	}

	public static void display(ActionBarChannel channel, String text, long ttlMs, Player... players)
	{
		for (Player player : players)
		{
			ActionBarService.display(player, channel, LegacyComponentSerializer.legacySection().deserialize(text == null ? "" : text), ttlMs);
		}
	}
	
	public static void displayProgress(double amount, Player... players)
	{
		displayProgress(null, amount, null, players);
	}
	
	public static void displayProgress(String prefix, double amount, Player... players)
	{
		displayProgress(prefix, amount, null, players);
	}
	
	public static void displayProgress(String prefix, double amount, String suffix, Player... players)
	{
		displayProgress(prefix, amount, suffix, false, players);
	}
	
	public static void displayProgress(String prefix, double amount, String suffix, boolean progressDirectionSwap, Player... players)
	{
		displayProgress(ActionBarChannel.LEGACY, prefix, amount, suffix, progressDirectionSwap, players);
	}

	public static void displayProgress(ActionBarChannel channel, double amount, Player... players)
	{
		displayProgress(channel, null, amount, null, players);
	}

	public static void displayProgress(ActionBarChannel channel, String prefix, double amount, Player... players)
	{
		displayProgress(channel, prefix, amount, null, players);
	}

	public static void displayProgress(ActionBarChannel channel, String prefix, double amount, String suffix, Player... players)
	{
		displayProgress(channel, prefix, amount, suffix, false, players);
	}

	public static void displayProgress(ActionBarChannel channel, String prefix, double amount, String suffix, boolean progressDirectionSwap, Player... players)
	{
		if (progressDirectionSwap)
			amount = 1 - amount;
		
		//Generate Bar
		int bars = 24;
		String progressBar = C.cGreen + "";
		boolean colorChange = false;
		for (int i=0 ; i<bars ; i++)
		{
			if (!colorChange && (float)i/(float)bars >= amount)
			{
				progressBar += C.cRed;
				colorChange = true;
			}
			
			progressBar += "▌";
		}
		
		//Build text
		String text = (prefix == null ? "" : prefix + ChatColor.RESET + " ") + progressBar + (suffix == null ? "" : ChatColor.RESET + " " + suffix);
		
		//Send to Player - use ActionBar for all players
		display(channel, text, players);
	} 
}
