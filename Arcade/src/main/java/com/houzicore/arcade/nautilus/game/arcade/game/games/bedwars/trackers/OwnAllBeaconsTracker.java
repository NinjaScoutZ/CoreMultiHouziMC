package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.trackers;

import org.bukkit.event.EventHandler;

import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.Bedwars;
import com.houzicore.arcade.nautilus.game.arcade.game.modules.capturepoint.CapturePoint;
import com.houzicore.arcade.nautilus.game.arcade.game.modules.capturepoint.CapturePointCaptureEvent;
import com.houzicore.arcade.nautilus.game.arcade.stats.StatTracker;

public class OwnAllBeaconsTracker extends StatTracker<Bedwars>
{

	public OwnAllBeaconsTracker(Bedwars game)
	{
		super(game);
	}

	@EventHandler
	public void capture(CapturePointCaptureEvent event)
	{
		GameTeam team = event.getPoint().getOwner();

		for (CapturePoint point : getGame().getCapturePointModule().getCapturePoints())
		{
			if (point.getOwner() == null || !point.getOwner().equals(team))
			{
				return;
			}
		}

		team.GetPlayers(true).forEach(player -> addStat(player, "OwnAllBeacons", 1, true, false));
	}

}
