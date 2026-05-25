package com.houzicore.shared.core.stats.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class StatChangeEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private final String _player;
	private final String _statName;
	private final long _valueBefore;

	private final long _valueAfter;

	public StatChangeEvent(String player, String statName, long valueBefore, long valueAfter) {
		_player = player;
		_statName = statName;
		_valueBefore = valueBefore;
		_valueAfter = valueAfter;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public String getPlayerName() {
		return _player;
	}

	public String getStatName() {
		return _statName;
	}

	public long getValueAfter() {
		return _valueAfter;
	}

	public long getValueBefore() {
		return _valueBefore;
	}
}
