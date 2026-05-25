package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.trackers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.Bedwars;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.event.BedwarsBreakBedEvent;
import com.houzicore.arcade.nautilus.game.arcade.stats.StatTracker;

public class Survive10Tracker extends StatTracker<Bedwars>
{

	private static final long TIME = TimeUnit.MINUTES.toMillis(10);

	private final Map<GameTeam, Long> _bedDestroyTime;

	public Survive10Tracker(Bedwars game)
	{
		super(game);

		_bedDestroyTime = new HashMap<>(8);
	}

	@EventHandler
	public void bedEat(BedwarsBreakBedEvent event)
	{
		_bedDestroyTime.put(event.getGameTeam(), System.currentTimeMillis());
	}

	@EventHandler
	public void updateAchievement(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		_bedDestroyTime.keySet().removeIf(gameTeam ->
		{
			long time = _bedDestroyTime.get(gameTeam);

			if (UtilTime.elapsed(time, TIME))
			{
				for (Player player : gameTeam.GetPlayers(true))
				{
					addStat(player, "Survive10", 1, true, false);
				}

				return true;
			}

			return false;
		});
	}
}
