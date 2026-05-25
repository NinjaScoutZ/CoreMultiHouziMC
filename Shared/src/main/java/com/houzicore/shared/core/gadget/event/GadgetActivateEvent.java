package com.houzicore.shared.core.gadget.event;

import com.houzicore.shared.core.gadget.types.Gadget;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GadgetActivateEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private final Player _player;

	private final Gadget _gadget;

	private boolean _cancelled = false;

	public GadgetActivateEvent(Player player, Gadget gadget) {
		_player = player;
		_gadget = gadget;
	}

	public Gadget getGadget() {
		return _gadget;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public Player getPlayer() {
		return _player;
	}

	public boolean isCancelled() {
		return _cancelled;
	}

	public void setCancelled(boolean cancel) {
		_cancelled = cancel;
	}
}
