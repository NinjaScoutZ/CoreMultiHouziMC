package nautilus.game.arcade.game.games.turfforts.mission;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.houzicore.shared.core.common.util.UtilEnt;
import com.houzicore.shared.core.mission.MissionTrackerType;

import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.missions.GameMissionTracker;

public class KillMidAirMissionTracker extends GameMissionTracker<Game>
{

	public KillMidAirMissionTracker(Game game)
	{
		super(MissionTrackerType.GAME_KILL_MIDAIR, game);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerDeath(PlayerDeathEvent event)
	{
		Player player = event.getEntity();
		Player killer = player.getKiller();

		if (killer == null || killer.equals(player) || UtilEnt.onBlock(killer))
		{
			return;
		}

		_manager.incrementProgress(killer, 1, _trackerType, getGameType(), null);
	}
}
