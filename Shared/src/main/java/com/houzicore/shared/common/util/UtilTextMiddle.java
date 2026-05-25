package com.houzicore.shared.common.util;

import org.bukkit.entity.Player;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import java.time.Duration;

public class UtilTextMiddle
{
	public static void display(String text, String subtitle, Player... players)
	{
		display(text, subtitle, 20, 60, 20, players);
	}
	
	public static void display(String text, String subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks, Player... players)
	{
		Title.Times times = Title.Times.times(
				Duration.ofMillis(fadeInTicks * 50L),
				Duration.ofMillis(stayTicks * 50L),
				Duration.ofMillis(fadeOutTicks * 50L)
		);
		
		Title title = Title.title(
				LegacyComponentSerializer.legacySection().deserialize(text == null ? "" : text),
				LegacyComponentSerializer.legacySection().deserialize(subtitle == null ? "" : subtitle),
				times
		);
		
		Player[] targets = (players == null || players.length == 0) ? UtilServer.getPlayers() : players;
		for (Player player : targets)
		{
			player.showTitle(title);
		}
	}
	
	public static void clear(Player... players)
	{
		for (Player player : players)
		{
			player.clearTitle();
		}
	}

	public static void reset(Player... players)
	{
		for (Player player : players)
		{
			player.resetTitle();
		}
	}

	public static String progress(float exp)
	{
		StringBuilder out = new StringBuilder();
		
		for (int i = 0; i < 40; i++)
		{
			float cur = i * (1f / 40f);
			
			if (cur < exp)
				out.append(C.cGreen).append(C.Bold).append("|");
			else
				out.append(C.cGray).append(C.Bold).append("|");
		}
		
		return out.toString();
	}
}

