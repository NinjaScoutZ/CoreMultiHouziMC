package com.houzicore.arcade.nautilus.game.arcade.stats;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.combat.event.CombatDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;

public class FoodForTheMassesStatTracker extends StatTracker<Game>
{
	public FoodForTheMassesStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCombatDeathEvent(CombatDeathEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
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
		if (player == null)
			return;

		if (event.GetLog().GetLastDamager() != null && event.GetLog().GetLastDamager().GetReason() != null && event.GetLog().GetLastDamager().GetReason().contains("Apple Thrower"))
		{
			addStat(killer, "FoodForTheMasses", 1, false, false);
		}
			
	}
}
