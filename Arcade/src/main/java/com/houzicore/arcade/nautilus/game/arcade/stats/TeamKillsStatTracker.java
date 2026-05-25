package com.houzicore.arcade.nautilus.game.arcade.stats;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.houzicore.shared.common.util.UtilPlayer;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;

public class TeamKillsStatTracker extends StatTracker<Game>
{
	public TeamKillsStatTracker(Game game)
	{
		super(game);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onCombatDeath(PlayerDeathEvent event)
	{
		if (getGame().GetState() != Game.GameState.Live)
			return;

		if (getGame().GetTeamList().size() < 2)
			return;

		if (event.getEntity().getKiller() == null)
			return;

		// getKiller is natively Player

		Player player = UtilPlayer.searchExact(event.getEntity().getKiller().getName());
		if (player == null)
			return;

		GameTeam team = getGame().GetTeam(player);

		if (team != null && team.GetName() != null)
			addStat(player, team.GetName() + " Kills", 1, false, false);
	}
}
