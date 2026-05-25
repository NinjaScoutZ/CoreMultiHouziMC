package com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.stattrackers;

import com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.SpeedBuilders;
import com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.events.PerfectBuildEvent;
import com.houzicore.arcade.nautilus.game.arcade.stats.StatTracker;

import org.bukkit.event.EventHandler;

public class DependableTracker extends StatTracker<SpeedBuilders>
{

	public DependableTracker(SpeedBuilders game)
	{
		super(game);
	}

	@EventHandler
	public void onPerfectBuild(PerfectBuildEvent event)
	{
		addStat(event.getPlayer(), "PerfectBuild", 1, false, false);
	}

}
