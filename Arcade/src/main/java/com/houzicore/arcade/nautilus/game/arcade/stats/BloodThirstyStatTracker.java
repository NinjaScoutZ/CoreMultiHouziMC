package com.houzicore.arcade.nautilus.game.arcade.stats;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.houzicore.shared.common.util.UtilPlayer;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.games.castlesiege.kits.KitUndead;
import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.kits.KitTracker;

public class BloodThirstyStatTracker extends StatTracker<Game>
{
	private final Map<UUID, Integer> _kills = new HashMap<>();

	public BloodThirstyStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCombatDeath(PlayerDeathEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		if (event.getEntity().getKiller() == null)
			return;

		// instanceof Player check not needed as getKiller returns Player natively

		Player killer = UtilPlayer.searchExact(event.getEntity().getKiller().getName());
		if (killer == null)
			return;

		if (event.getEntity() == null)
			return;

		// instanceof Player check not needed as getEntity returns Player natively

		Player player = UtilPlayer.searchExact(event.getEntity().getName());
		if (player == null)
			return;

		if (getGame().GetKit(player) instanceof KitUndead)
		{
			Integer kills = _kills.get(killer.getUniqueId());

			kills = (kills == null ? 0 : kills) + 1;

			_kills.put(killer.getUniqueId(), kills);

			if (kills >= 50)
				addStat(killer, "BloodThirsty", 1, true, false);
		}
	}
}
