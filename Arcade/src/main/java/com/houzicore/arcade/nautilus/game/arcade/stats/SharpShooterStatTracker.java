package com.houzicore.arcade.nautilus.game.arcade.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;

public class SharpShooterStatTracker extends StatTracker<Game>
{
	private final Map<UUID, Integer> _arrowsShot = new HashMap<>();
	private final Map<UUID, Integer> _arrowsHit = new HashMap<>();

	public SharpShooterStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onProjectileLaunch(ProjectileLaunchEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		if (event.getEntity().getShooter() instanceof Player && event.getEntity() instanceof Arrow)
		{
			Player player = (Player) event.getEntity().getShooter();

			Integer count = _arrowsShot.get(player.getUniqueId());
			count = (count == null ? 0 : count) + 1;
			_arrowsShot.put(player.getUniqueId(), count);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onArrowHit(EntityDamageByEntityEvent event)
	{
/*
		if (getGame().GetState() != Game.GameState.Live)
			return;

		if (event.getDamager() instanceof Arrow && ((Player) event.getEntity()) != null)
		{
// 			if (event.getDamager().getShooter() instanceof Player && event.getDamager().getShooter() != ((Player) event.getEntity()))
			{
// 				Player player = (Player) event.getDamager().getShooter();

				Integer count = _arrowsHit.get(player.getUniqueId());
				count = (count == null ? 0 : count) + 1;
				_arrowsHit.put(player.getUniqueId(), count);

				if (count == 8)
				{
					Integer arrowsShot = _arrowsShot.get(player.getUniqueId());

					if (arrowsShot != null && arrowsShot == 8)
						addStat(player, "Sharpshooter", 1, true, false);
				}
			}
		}
*/
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		_arrowsShot.remove(event.getEntity().getUniqueId());
		_arrowsHit.remove(event.getEntity().getUniqueId());
	}
}
