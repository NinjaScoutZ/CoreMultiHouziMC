package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.trackers;

import org.bukkit.event.EventHandler;

import com.houzicore.arcade.nautilus.game.arcade.events.FirstBloodEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.stats.StatTracker;

public class FirstBloodStatTracker extends StatTracker<Game>
{

	public FirstBloodStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler
	public void firstBlood(FirstBloodEvent event)
	{
		addStat(event.getPlayer(), "FirstBlood", 1, true, false);
	}
}
