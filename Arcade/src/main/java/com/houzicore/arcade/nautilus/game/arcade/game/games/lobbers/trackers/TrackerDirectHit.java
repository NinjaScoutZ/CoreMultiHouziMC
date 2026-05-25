package com.houzicore.arcade.nautilus.game.arcade.game.games.lobbers.trackers;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.stats.StatTracker;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class TrackerDirectHit extends StatTracker<Game>
{
	public TrackerDirectHit(Game game)
	{
		super(game);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onKillDirectHit(EntityDamageByEntityEvent event)
	{
		if (!getGame().IsLive())
			return;
		
		if (((Player) event.getDamager()) == null)
			return;
		
		if (!(event.getDamager() instanceof Player)) return;
		Player damager = ((Player) event.getDamager());
		
		if (!getGame().IsAlive(damager))
			return;
			
		if (event.getCause().name() == null)
			return;
		
		if (event.getCause().name().toLowerCase().contains("direct hit"))
			addStat(damager, "Direct Hit", 1, false, false);
	}
}
