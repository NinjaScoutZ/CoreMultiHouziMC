package com.houzicore.arcade.nautilus.game.arcade.kit.perks.data;

import org.bukkit.entity.Player;

public class EarthquakeData
{
	public Player Player;
	public long Time;

	public EarthquakeData(Player player)
	{
		Player = player;
		Time = System.currentTimeMillis();
	}
}
