package com.houzicore.arcade.nautilus.game.arcade.stats;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.houzicore.shared.common.util.UtilPlayer;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.TeamGame;
import com.houzicore.arcade.nautilus.game.arcade.game.games.minestrike.MineStrike;

public class KillAllOpposingMineStrikeRoundStatTracker extends StatTracker<MineStrike>
{
	private final Map<UUID, Set<UUID>> _kills = new HashMap<>();

	public KillAllOpposingMineStrikeRoundStatTracker(MineStrike game)
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

		Player killer = UtilPlayer.searchExact(event.getEntity().getKiller().getName());
		if (killer == null)
			return;

		if (event.getEntity() == null)
			return;

		// getEntity is natively Player

		Player killed = UtilPlayer.searchExact(event.getEntity().getName());
		if (killed == null)
			return;

		Set<UUID> kills = _kills.get(killer.getUniqueId());
		if (kills == null)
		{
			kills = new HashSet<>();
			_kills.put(killer.getUniqueId(), kills);
		}

		kills.add(killed.getUniqueId());
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onRoundOver(MineStrike.RoundOverEvent event)
	{
		for (GameTeam team : getGame().GetTeamList())
		{
			for (Player player : team.GetPlayers(false))
			{
				Set<UUID> kills = _kills.get(player.getUniqueId());
				if (kills == null)
					continue;

				for (GameTeam otherTeam : getGame().GetTeamList())
				{
					if (otherTeam == team)
						continue;

					boolean killedAll = true;

					for (Player otherPlayer : otherTeam.GetPlayers(true))
					{
						if (!kills.contains(otherPlayer.getUniqueId()))
						{
							killedAll = false;

							break;
						}
					}

					if (killedAll)
						addStat(player, "Ace", 1, true, false);
				}
			}
		}

		_kills.clear();
	}
}
