package com.houzicore.shared.core.mount.event;

import com.houzicore.shared.core.mount.Mount;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MountActivateEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private final Player _player;

	private final Mount<?> _mount;

	private boolean _cancelled = false;

	public MountActivateEvent(Player player, Mount<?> mount) {
		_player = player;
		_mount = mount;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public Mount getMount() {
		return _mount;
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
