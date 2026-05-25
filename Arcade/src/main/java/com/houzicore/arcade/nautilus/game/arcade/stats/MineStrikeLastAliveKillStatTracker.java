package com.houzicore.arcade.nautilus.game.arcade.stats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.houzicore.shared.common.util.UtilPlayer;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.games.minestrike.MineStrike;

public class MineStrikeLastAliveKillStatTracker extends StatTracker<MineStrike>
{
	private final Map<UUID, Integer> _killCount = new HashMap<>();

	public MineStrikeLastAliveKillStatTracker(MineStrike game)
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

		// getKiller is natively Player

		Player player = UtilPlayer.searchExact(event.getEntity().getKiller().getName());
		if (player == null)
			return;

		Integer killCount = _killCount.get(player.getUniqueId());
		_killCount.put(player.getUniqueId(), (killCount == null ? 0 : killCount) + 1);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onRoundOver(MineStrike.RoundOverEvent event)
	{
		for (GameTeam team : getGame().GetTeamList())
		{
			List<Player> players = team.GetPlayers(true);

			if (players.size() == 1)
			{
				Player player = players.get(0);
				Integer killCount = _killCount.get(player.getUniqueId());

				if (killCount != null && killCount >= 3)
					addStat(player, "ClutchOrKick", 1, true, false);
			}
		}

		_killCount.clear();
	}
}
