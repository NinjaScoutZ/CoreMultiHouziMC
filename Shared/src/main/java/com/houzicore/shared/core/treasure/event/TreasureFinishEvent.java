package com.houzicore.shared.core.treasure.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.houzicore.shared.core.treasure.Treasure;

/**
 * Created by shaun on 14-09-12.
 */
public class TreasureFinishEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}
	private final Player _player;

	private final Treasure _treasure;

	public TreasureFinishEvent(Player player, Treasure treasure) {
		_player = player;
		_treasure = treasure;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public Player getPlayer() {
		return _player;
	}

	public Treasure getTreasure() {
		return _treasure;
	}
}
