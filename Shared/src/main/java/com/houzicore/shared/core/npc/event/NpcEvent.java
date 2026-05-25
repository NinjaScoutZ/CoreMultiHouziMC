package com.houzicore.shared.core.npc.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NpcEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private final LivingEntity _npc;

	private boolean _cancelled = false;

	public NpcEvent(LivingEntity npc) {
		_npc = npc;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public LivingEntity getNpc() {
		return _npc;
	}

	public boolean isCancelled() {
		return _cancelled;
	}

	public void setCancelled(boolean cancel) {
		_cancelled = cancel;
	}
}
