package com.houzicore.shared.core.gadget.event;

import com.houzicore.shared.core.gadget.types.ItemGadget;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ItemGadgetOutOfAmmoEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private final Player _player;

	private final ItemGadget _gadget;

	public ItemGadgetOutOfAmmoEvent(Player player, ItemGadget gadget) {
		_player = player;
		_gadget = gadget;
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
}
