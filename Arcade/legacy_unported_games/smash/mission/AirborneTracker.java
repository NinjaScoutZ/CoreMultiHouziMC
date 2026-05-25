package nautilus.game.arcade.game.games.smash.mission;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.houzicore.shared.core.common.util.UtilEnt;
import com.houzicore.shared.core.common.util.UtilPlayer;
import com.houzicore.shared.core.mission.MissionTrackerType;
import com.houzicore.shared.core.updater.UpdateType;
import com.houzicore.shared.core.updater.event.UpdateEvent;

import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.missions.GameMissionTracker;

public class AirborneTracker extends GameMissionTracker<Game>
{

	public AirborneTracker(Game game)
	{
		super(MissionTrackerType.SSM_AIRBORNE, game);
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		for (Player player : _game.GetPlayers(true))
		{
			if (UtilPlayer.isSpectator(player) || UtilEnt.onBlock(player))
			{
				continue;
			}

			_manager.incrementProgress(player, 1, _trackerType, getGameType(), null);
		}
	}

}
