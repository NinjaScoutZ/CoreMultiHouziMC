package com.houzicore.shared.updater.event;

import com.houzicore.shared.updater.UpdateType;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class UpdateEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private final UpdateType _type;

	public UpdateEvent(UpdateType example) {
		_type = example;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public UpdateType getType() {
		return _type;
	}
}
