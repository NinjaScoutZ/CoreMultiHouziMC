package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.capturepoint;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;

import com.houzicore.shared.common.util.F;

import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.BedwarsModule;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.Bedwars;
import com.houzicore.arcade.nautilus.game.arcade.game.modules.capturepoint.CapturePoint;
import com.houzicore.arcade.nautilus.game.arcade.game.modules.capturepoint.CapturePointCaptureEvent;

public class BedwarsPointModule extends BedwarsModule
{

	public BedwarsPointModule(Bedwars game)
	{
		super(game);
	}

	@EventHandler
	public void pointCapture(CapturePointCaptureEvent event)
	{
		GameTeam team = event.getPoint().getOwner();

		for (Player player : team.GetPlayers(true))
		{
			_game.AddGems(player, 2, "Team - Beacons captured", true, true);
		}
	}

	@EventHandler
	public void blockPlace(BlockPlaceEvent event)
	{
		if (!_game.IsLive())
		{
			return;
		}

		Location location = event.getBlock().getLocation().add(0.5, 0, 0.5);

		for (CapturePoint point : _game.getCapturePointModule().getCapturePoints())
		{
			if (point.isOnPoint(location))
			{
				event.getPlayer().sendMessage(F.main("Game", "You cannot place blocks near a beacon."));
				event.setCancelled(true);
				return;
			}
		}
	}

	public int ownedDiamondPoints(GameTeam team)
	{
		return ownedPoints(team, ChatColor.GOLD);
	}

	public int ownedEmeraldPoints(GameTeam team)
	{
		return ownedPoints(team, ChatColor.GREEN);
	}

	private int ownedPoints(GameTeam team, ChatColor colour)
	{
		int i = 0;

		for (CapturePoint point : _game.getCapturePointModule().getCapturePoints())
		{
			if (point.getColour().equals(colour) && point.getOwner() != null && point.getOwner().equals(team))
			{
				i++;
			}
		}

		return i;
	}
}
