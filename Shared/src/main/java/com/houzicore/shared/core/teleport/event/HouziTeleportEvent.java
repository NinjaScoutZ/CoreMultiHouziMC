package com.houzicore.shared.core.teleport.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HouziTeleportEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private final Player _entity;

	private final Location _loc;

	private boolean _cancelled = false;

	public HouziTeleportEvent(Player entity, Location loc) {
		_entity = entity;
		_loc = loc;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public Location getLocation() {
		return _loc;
	}

	public Player getPlayer() {
		return _entity;
	}

	public boolean isCancelled() {
		return _cancelled;
	}

	public void setCancelled(boolean cancel) {
		_cancelled = cancel;
	}
}
