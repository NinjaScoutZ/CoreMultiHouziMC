package com.houzicore.shared.core.gadget.event;

import java.util.List;

import com.houzicore.shared.core.gadget.types.Gadget;

import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GadgetBlockEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private final Gadget _gadget;

	private final List<Block> _blocks;

	private boolean _cancelled = false;

	public GadgetBlockEvent(Gadget gadget, List<Block> blocks) {
		_gadget = gadget;
		_blocks = blocks;
	}

	public List<Block> getBlocks() {
		return _blocks;
	}

	public Gadget getGadget() {
		return _gadget;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public boolean isCancelled() {
		return _cancelled;
	}

	public void setCancelled(boolean cancel) {
		_cancelled = cancel;
	}
}
