package com.houzicore.shared.core.gadget.event;

import com.houzicore.shared.core.gadget.types.ItemGadget;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ItemGadgetUseEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private final Player _player;
	private final ItemGadget _gadget;

	private final int _count;

	private boolean _cancelled = false;

	public ItemGadgetUseEvent(Player player, ItemGadget gadget, int count) {
		_player = player;
		_gadget = gadget;
		_count = count;
	}

	public int getCount() {
		return _count;
	}

	public ItemGadget getGadget() {
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
