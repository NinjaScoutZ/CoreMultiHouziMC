package com.houzicore.shared.core.cosmetic.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ActivateEssenceBoosterEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private final Player _player;

	private boolean _cancelled = false;

	public ActivateEssenceBoosterEvent(Player player) {
		_player = player;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public Player getPlayer() {
		return _player;
	}

	public boolean isCancelled() {
		return _cancelled;
	}

	public void setCancelled(boolean cancel) {
		_cancelled = cancel;
	}
}
