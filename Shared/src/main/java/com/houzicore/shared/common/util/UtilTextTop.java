package com.houzicore.shared.common.util;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UtilTextTop 
{
	private static final Map<UUID, BossBar> _bars = new HashMap<>();
	private static final Map<UUID, BukkitTask> _tasks = new HashMap<>();

	public static void display(String text, Player... players)
	{
		displayProgress(text, 1.0, BarColor.BLUE, players);
	}
	
	public static void displayProgress(String text, double progress, Player... players)
	{
		displayProgress(text, progress, BarColor.BLUE, players);
	}

	public static void displayProgress(String text, double progress, BarColor color, Player... players)
	{
		for (Player player : players)
			displayTextBar(player, progress, text, color);
	}
	
	public static void displayTextBar(final Player player, double healthPercent, String text)
	{
		displayTextBar(player, healthPercent, text, BarColor.BLUE);
	}

	public static void displayTextBar(final Player player, double healthPercent, String text, BarColor color)
	{
		BossBar bar = _bars.get(player.getUniqueId());
		if (bar == null)
		{
			bar = Bukkit.createBossBar(text, color, BarStyle.SOLID);
			bar.addPlayer(player);
			_bars.put(player.getUniqueId(), bar);
		}
		
		bar.setTitle(text);
		bar.setColor(color);
		bar.setProgress(Math.max(0, Math.min(1, healthPercent)));
		bar.setVisible(true);

		// Cancel the old removal task
		BukkitTask oldTask = _tasks.get(player.getUniqueId());
		if (oldTask != null)
		{
			oldTask.cancel();
		}

		// Schedule a new removal task (wait 60 ticks before hiding so it doesn't flicker on small delays)
		BukkitTask newTask = Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugins()[0], () -> {
			BossBar b = _bars.remove(player.getUniqueId());
			_tasks.remove(player.getUniqueId());
			if (b != null)
			{
				b.removeAll();
			}
		}, 40);

		_tasks.put(player.getUniqueId(), newTask);
	}
}
