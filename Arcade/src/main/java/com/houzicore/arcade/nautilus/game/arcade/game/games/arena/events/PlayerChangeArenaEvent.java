package com.houzicore.arcade.nautilus.game.arcade.game.games.arena.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.houzicore.arcade.nautilus.game.arcade.game.games.arena.ArenaNode;

public class PlayerChangeArenaEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private final Player player;
	private final ArenaNode from;
	private final ArenaNode to;

	public PlayerChangeArenaEvent(Player player, ArenaNode from, ArenaNode to) {
		this.player = player;
		this.from = from;
		this.to = to;
	}

	public Player getPlayer() {
		return player;
	}

	public ArenaNode getFrom() {
		return from;
	}

	public ArenaNode getTo() {
		return to;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
