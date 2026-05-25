package com.houzicore.shared.account.event;

import com.houzicore.shared.account.CoreClient;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GetClientEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private String _name;

	private CoreClient _client;

	public GetClientEvent(Player player) {
		_name = player.getName();
	}

	public GetClientEvent(String name) {
		_name = name;
	}

	public CoreClient GetClient() {
		return _client;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public String GetName() {
		return _name;
	}

	public void SetClient(CoreClient client) {
		_client = client;
	}

	public void SetName(String name) {
		_name = name;
	}
}
