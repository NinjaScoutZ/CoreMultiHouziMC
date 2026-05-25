package com.houzicore.shared.core.pet.event;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PetSpawnEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private boolean _cancelled = false;
	private final Player _player;
	private final EntityType _entityType;

	private final Location _location;

	public PetSpawnEvent(Player player, EntityType entityType, Location location) {
		_player = player;
		_entityType = entityType;
		_location = location;
	}

	public EntityType GetEntityType() {
		return _entityType;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public Location GetLocation() {
		return _location;
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
