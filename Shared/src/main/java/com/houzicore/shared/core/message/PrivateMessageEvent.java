package com.houzicore.shared.core.message;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PrivateMessageEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private boolean _cancelled = false;
	private final Player _sender;
	private final Player _recipient;

	private final String _msg;

	public PrivateMessageEvent(Player sender, Player recipient, String msg) {
		_sender = sender;
		_recipient = recipient;
		_msg = msg;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public String getMessage() {
		return _msg;
	}

	public Player getRecipient() {
		return _recipient;
	}

	public Player getSender() {
		return _sender;
	}

	public boolean isCancelled() {
		return _cancelled;
	}

	public void setCancelled(boolean cancel) {
		_cancelled = cancel;
	}
}
