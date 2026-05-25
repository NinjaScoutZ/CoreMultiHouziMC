package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.trackers;

import java.util.concurrent.TimeUnit;

import org.bukkit.event.EventHandler;

import com.houzicore.shared.common.util.UtilTime;

import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.Bedwars;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.event.BedwarsBreakBedEvent;
import com.houzicore.arcade.nautilus.game.arcade.stats.StatTracker;

public class BreakFirstMinuteTracker extends StatTracker<Bedwars>
{

	private static final long TIME = TimeUnit.MINUTES.toMillis(1);

	public BreakFirstMinuteTracker(Bedwars game)
	{
		super(game);
	}

	@EventHandler
	public void bedEat(BedwarsBreakBedEvent event)
	{
		if (!UtilTime.elapsed(getGame().GetStateTime(), TIME))
		{
			addStat(event.getPlayer(), "Eat1", 1, true, false);
		}
	}
}
