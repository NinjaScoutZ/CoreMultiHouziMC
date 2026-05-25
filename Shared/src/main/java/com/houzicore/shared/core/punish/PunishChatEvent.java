package com.houzicore.shared.core.punish;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PunishChatEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private boolean _cancelled = false;

	private final Player _player;

	public PunishChatEvent(Player player) {
		_player = player;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public Player GetPlayer() {
		return _player;
	}

	@Override
	public boolean isCancelled() {
		return _cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		_cancelled = cancel;
	}
}
