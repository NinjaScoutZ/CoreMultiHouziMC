package com.houzicore.arcade.nautilus.game.arcade.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class FirstBloodEvent extends Event
{

	private Player player;

	public FirstBloodEvent(Player player)
	{
		this.player = player;
	}

	public Player getPlayer()
	{
		return player;
	}

	private static HandlerList _handlers = new HandlerList();

	public static HandlerList getHandlerList()
	{
		return _handlers;
	}

	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}

}
