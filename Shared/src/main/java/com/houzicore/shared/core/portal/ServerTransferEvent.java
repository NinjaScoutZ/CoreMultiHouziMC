package com.houzicore.shared.core.portal;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ServerTransferEvent extends Event {

	private static final HandlerList _handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return _handlers;
	}

	private final Player _player;

	private final String _server;

	public ServerTransferEvent(Player player, String server) {
		_player = player;
		_server = server;
	}

	@Override
	public HandlerList getHandlers() {
		return _handlers;
	}

	public Player getPlayer() {
		return _player;
	}

	public String getServer() {
		return _server;
	}

}
