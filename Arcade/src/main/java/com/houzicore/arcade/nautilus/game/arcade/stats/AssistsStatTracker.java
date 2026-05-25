package com.houzicore.arcade.nautilus.game.arcade.stats;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.houzicore.arcade.nautilus.game.arcade.game.Game;

public class AssistsStatTracker extends StatTracker<Game>
{
	private final HashMap<Player, HashMap<Player, Long>> _damageLog = new HashMap<>();

	public AssistsStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDamage(EntityDamageByEntityEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		if (!(event.getEntity() instanceof Player)) return;
		if (!(event.getDamager() instanceof Player)) return;

		Player victim = (Player) event.getEntity();
		Player attacker = (Player) event.getDamager();

		if (victim.equals(attacker)) return;

		_damageLog.putIfAbsent(victim, new HashMap<>());
		_damageLog.get(victim).put(attacker, System.currentTimeMillis());
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCombatDeath(PlayerDeathEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		Player victim = event.getEntity();
		Player killer = victim.getKiller();

		HashMap<Player, Long> attackers = _damageLog.remove(victim);
		if (attackers == null) return;

		long now = System.currentTimeMillis();
		for (Map.Entry<Player, Long> entry : attackers.entrySet())
		{
			Player attacker = entry.getKey();
			if (attacker.equals(killer)) continue;
			
			// 10 second assist window
			if (now - entry.getValue() > 10000) continue;

			addStat(attacker, "Assists", 1, false, false);

			if (getGame().GetKit(attacker) != null)
			{
				addStat(attacker, getGame().GetKit(attacker).GetName() + " Assists", 1, false, false);
			}
		}
	}
}
