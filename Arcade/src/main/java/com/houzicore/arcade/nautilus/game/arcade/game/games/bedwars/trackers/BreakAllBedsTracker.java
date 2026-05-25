package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.trackers;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.Bedwars;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.event.BedwarsBreakBedEvent;
import com.houzicore.arcade.nautilus.game.arcade.stats.StatTracker;

public class BreakAllBedsTracker extends StatTracker<Bedwars>
{

	private Player _lastPlayer;
	private boolean _award;

	public BreakAllBedsTracker(Bedwars game)
	{
		super(game);

		_award = true;
	}

	@EventHandler
	public void bedEat(BedwarsBreakBedEvent event)
	{
		if (!_award)
		{
			return;
		}

		Player player = event.getPlayer();

		if (_lastPlayer == null)
		{
			_lastPlayer = player;
		}
		else if (!_lastPlayer.equals(player))
		{
			_award = false;
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onGameStateChange(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.End || _lastPlayer == null || !_award)
		{
			return;
		}

		addStat(_lastPlayer, "FinalBite", 1, true, false);
	}
}
