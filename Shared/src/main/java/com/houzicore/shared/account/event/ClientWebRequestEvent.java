package com.houzicore.shared.account.event;

import com.google.gson.stream.JsonWriter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ClientWebRequestEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private final JsonWriter _writer;

	public ClientWebRequestEvent(JsonWriter writer) {
		_writer = writer;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public JsonWriter GetJsonWriter() {
		return _writer;
	}
}
