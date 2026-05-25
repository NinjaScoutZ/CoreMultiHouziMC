package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BedRotEvent extends Event
{

	private static final HandlerList HANDLER_LIST = new HandlerList();

	public static HandlerList getHandlerList()
	{
		return HANDLER_LIST;
	}

	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}

}
