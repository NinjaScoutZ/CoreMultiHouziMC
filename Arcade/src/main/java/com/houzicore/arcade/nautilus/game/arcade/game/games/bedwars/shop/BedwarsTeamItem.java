package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.houzicore.shared.common.Pair;

public interface BedwarsTeamItem extends BedwarsItem
{

	void apply(Player player, int level, Location bed);

	String getName();

	String[] getDescription(int level);

	Pair<String, Integer>[] getLevels();

}
