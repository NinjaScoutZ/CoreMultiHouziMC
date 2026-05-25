package com.houzicore.shared.core.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by Shaun on 10/24/2014.
 */
public class CustomTagEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private final Player _player;
	private final int _entityId;

	private String _customName;

	public CustomTagEvent(Player player, int entityId, String customName) {
		_player = player;
		_entityId = entityId;
		_customName = customName;
	}

	public String getCustomName() {
		return _customName;
	}

	public int getEntityId() {
		return _entityId;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public Player getPlayer() {
		return _player;
	}

	public void setCustomName(String customName) {
		_customName = customName;
	}
}
