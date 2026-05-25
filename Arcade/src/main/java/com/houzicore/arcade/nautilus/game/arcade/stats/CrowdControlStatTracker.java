package com.houzicore.arcade.nautilus.game.arcade.stats;

import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;

public class CrowdControlStatTracker extends StatTracker<Game>
{
	private HashMap<Player, HashMap<Player, Long>> _hits = new HashMap<>();

	public CrowdControlStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onDamage(EntityDamageByEntityEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player))
			return;

		Player damager = (Player) event.getDamager();
		Player victim = (Player) event.getEntity();

		GameTeam dTeam = getGame().GetTeam(damager);
		GameTeam vTeam = getGame().GetTeam(victim);

		if (dTeam == null || vTeam == null || !dTeam.GetName().equals("Hiders") || !vTeam.GetName().equals("Hunters"))
			return;

		if (!_hits.containsKey(damager))
			_hits.put(damager, new HashMap<>());

		_hits.get(damager).put(victim, System.currentTimeMillis());

		int count = 0;
		long now = System.currentTimeMillis();
		for (Long time : _hits.get(damager).values())
		{
			if (now - time < 1500)
			{
				count++;
			}
		}

		if (count >= 3)
		{
			addStat(damager, "CrowdControl", 1, true, false);
		}
	}
}
