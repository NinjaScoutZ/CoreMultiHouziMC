package com.houzicore.shared.core.creature.event;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CreatureSpawnCustomEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private boolean _cancelled = false;

	private final Location _location;

	public CreatureSpawnCustomEvent(Location location) {
		_location = location;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public Location GetLocation() {
		return _location;
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
