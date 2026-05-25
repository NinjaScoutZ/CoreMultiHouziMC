package com.houzicore.shared.core.scoreboard;

import java.util.List;
import org.bukkit.entity.Player;
import net.kyori.adventure.text.Component;

/**
 * Interface to fetch title and lines for scoreboard sidebar rendering.
 */
public interface ScoreboardDataProvider {
	Component getTitle(Player player);
	List<Component> getLines(Player player);
}
