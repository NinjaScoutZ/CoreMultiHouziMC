package com.houzicore.shared.recharge;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RechargedEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private final Player _player;

	private final String _ability;

	public RechargedEvent(Player player, String ability) {
		_player = player;
		_ability = ability;
	}

	public String GetAbility() {
		return _ability;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public Player GetPlayer() {
		return _player;
	}
}
