package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.team.BedwarsTeam;

public class BedwarsBreakBedEvent extends PlayerEvent
{

	private static final HandlerList HANDLER_LIST = new HandlerList();

	private final BedwarsTeam _bedTeam;

	public BedwarsBreakBedEvent(Player who, BedwarsTeam bedTeam)
	{
		super(who);

		_bedTeam = bedTeam;
	}

	public BedwarsTeam getBedwarsTeam()
	{
		return _bedTeam;
	}

	public GameTeam getGameTeam()
	{
		return _bedTeam.getGameTeam();
	}

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
