package com.houzicore.shared.account.event;

import java.util.UUID;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ClientWebResponseEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private final String _response;

	private final UUID _uuid;

	public ClientWebResponseEvent(String response, UUID uuid) {
		_response = response;
		_uuid = uuid;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public String GetResponse() {
		return _response;
	}

	public UUID getUniqueId() {
		return _uuid;
	}
}
