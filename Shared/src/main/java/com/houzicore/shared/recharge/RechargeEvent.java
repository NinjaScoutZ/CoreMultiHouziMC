package com.houzicore.shared.recharge;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RechargeEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private final Player _player;
	private final String _ability;

	private long _recharge;

	public RechargeEvent(Player player, String ability, long recharge) {
		super(!org.bukkit.Bukkit.isPrimaryThread());
		_player = player;
		_ability = ability;
		_recharge = recharge;
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

	public long GetRecharge() {
		return _recharge;
	}

	public void SetRecharge(long time) {
		_recharge = time;
	}
}
