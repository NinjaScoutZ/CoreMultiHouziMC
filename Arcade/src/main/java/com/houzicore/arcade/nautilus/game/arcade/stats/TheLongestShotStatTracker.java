package com.houzicore.arcade.nautilus.game.arcade.stats;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.combat.event.CombatDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;

public class TheLongestShotStatTracker extends StatTracker<Game>
{
	public TheLongestShotStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCombatDeath(CombatDeathEvent event)
	{
		if (!getGame().IsLive())
			return;

		if (event.GetEvent().getEntity().getKiller() == null)
			return;

		// getKiller is natively Player

		Player killer = UtilPlayer.searchExact(event.GetEvent().getEntity().getKiller().getName());
		if (killer == null)
			return;

		if (event.GetEvent().getEntity() == null)
			return;

		// getEntity is natively Player

		Player player = UtilPlayer.searchExact(event.GetEvent().getEntity().getName());
		
		if (player == null || !player.isOnline())
			return;

		if (event.GetLog().GetLastDamager() != null && event.GetLog().GetLastDamager().GetReason() != null && event.GetLog().GetLastDamager().GetReason().toLowerCase().contains("longshot"))
		{
			if (killer.getLocation().distance(player.getLocation()) >= 64)
			{
				addStat(killer, "TheLongestShot", 1, false, false);
			}
		}
	}
}
