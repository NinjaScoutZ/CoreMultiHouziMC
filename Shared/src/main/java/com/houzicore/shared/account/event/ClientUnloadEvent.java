package com.houzicore.shared.account.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ClientUnloadEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private final String _name;

	public ClientUnloadEvent(String name) {
		_name = name;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public String GetName() {
		return _name;
	}
}
