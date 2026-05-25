package com.houzicore.shared.core.scoreboard;

import net.kyori.adventure.text.ComponentLike;
import net.megavex.scoreboardlibrary.api.objective.ScoreFormat;
import net.megavex.scoreboardlibrary.api.sidebar.Sidebar;
import org.bukkit.entity.Player;

/**
 * HouziCore-owned wrapper around scoreboard-library Sidebar so shaded package
 * relocation never leaks into cross-module method signatures.
 */
public final class ScoreboardSidebar
{
	public static final int MAX_LINES = Sidebar.MAX_LINES;

	private final Sidebar _sidebar;

	ScoreboardSidebar(Sidebar sidebar)
	{
		_sidebar = sidebar;
	}

	public void addPlayer(Player player)
	{
		_sidebar.addPlayer(player);
	}

	public void removePlayer(Player player)
	{
		_sidebar.removePlayer(player);
	}

	public void title(ComponentLike title)
	{
		_sidebar.title(title);
	}

	public void line(int index, ComponentLike line)
	{
		_sidebar.line(index, line);
	}

	public void lineWithoutScore(int index, ComponentLike line)
	{
		_sidebar.line(index, line, ScoreFormat.blank());
	}

	public void clearLines()
	{
		_sidebar.clearLines();
	}

	public void close()
	{
		_sidebar.close();
	}

	public boolean closed()
	{
		return _sidebar.closed();
	}
}
