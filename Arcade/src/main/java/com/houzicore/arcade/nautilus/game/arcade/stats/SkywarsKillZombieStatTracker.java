package com.houzicore.arcade.nautilus.game.arcade.stats;

import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.games.skywars.events.PlayerKillZombieEvent;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class SkywarsKillZombieStatTracker extends StatTracker<Game>
{

	public SkywarsKillZombieStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onKillZombie(PlayerKillZombieEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		if (!(event.getWho() instanceof Player))
		{
			return;
		}

		if (!(event.getZombie() instanceof LivingEntity))
		{
			return;
		}

		addStat(event.getWho(), "ZombieKills", 1, false, false);
	}

}
