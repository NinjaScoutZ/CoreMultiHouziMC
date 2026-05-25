package com.houzicore.shared.core.creature.event;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CreatureKillEntitiesEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private boolean _cancelled = false;

	private final List<Entity> _entities;

	public CreatureKillEntitiesEvent(List<Entity> entities) {
		_entities = entities;
	}

	public List<Entity> GetEntities() {
		return _entities;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
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
