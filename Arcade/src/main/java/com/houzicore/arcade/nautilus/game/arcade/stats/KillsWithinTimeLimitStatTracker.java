package com.houzicore.arcade.nautilus.game.arcade.stats;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.houzicore.shared.common.util.*;
//import com.houzicore.shared.combat.event.*;
import com.houzicore.arcade.nautilus.game.arcade.game.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;

import java.util.*;

public class KillsWithinTimeLimitStatTracker extends StatTracker<Game>
{
	private final int _killCount;
	private final String _stat;
	private final int _timeLimit;

	private final Map<UUID, Integer> _kills = new HashMap<>();

	public KillsWithinTimeLimitStatTracker(Game game, int killCount, int timeLimit, String stat)
	{
		super(game);

		_killCount = killCount;
		_stat = stat;
		_timeLimit = timeLimit * 1000;
	}

	public int getKillCount()
	{
		return _killCount;
	}

	public int getTimeLimit()
	{
		return _timeLimit;
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

		Integer killCount = _kills.get(player.getUniqueId());

		killCount = (killCount == null ? 0 : killCount) + 1;

		_kills.put(player.getUniqueId(), killCount);

		if (killCount == getKillCount() && System.currentTimeMillis() - getGame().GetStateTime() < getTimeLimit())
			addStat(player, getStat(), 1, true, false);
	}

	public String getStat()
	{
		return _stat;
	}
}
