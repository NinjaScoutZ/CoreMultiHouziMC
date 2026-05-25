package com.houzicore.shared.core.gadget.event;

import com.houzicore.shared.core.gadget.types.Gadget;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GadgetCollideEntityEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private final Gadget _gadget;

	private final Entity _other;

	private boolean _cancelled = false;

	public GadgetCollideEntityEvent(Gadget gadget, Entity other) {
		_gadget = gadget;
		_other = other;
	}

	public Gadget getGadget() {
		return _gadget;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public Entity getOther() {
		return _other;
	}

	public boolean isCancelled() {
		return _cancelled;
	}

	public void setCancelled(boolean cancel) {
		_cancelled = cancel;
	}
}
