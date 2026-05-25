package com.houzicore.arcade.nautilus.game.arcade.game.modules.capturepoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilFirework;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;

import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;

public class CapturePoint
{

	static final int MAX_RADIUS = 5;
	private static final int MAX_PROGRESS = 5;
	private static final int MAX_PROGRESS_NEUTRAL = 10;
	private static final int MIN_INFORM_TIME = (int) TimeUnit.SECONDS.toMillis(30);

	private final Game _host;

	private final String _name;
	private final ChatColor _colour;

	private final Location _center;
	private final List<Block> _wool;
	private final List<Block> _changed;

	private final double _captureDist;

	private GameTeam _owner;
	private GameTeam _side;
	private int _progress;
	private long _lastInform;

	CapturePoint(Game host, String name, ChatColor colour, Location center)
	{
		_host = host;
		_name = name;
		_colour = colour;
		_center = center;
		_wool = new ArrayList<>(36);
		_changed = new ArrayList<>(_wool.size());

		double highestDist = 0;

		for (Entry<Block, Double> entry : UtilBlock.getInRadius(center, MAX_RADIUS).entrySet())
		{
			Block block = entry.getKey();
			double offset = entry.getValue();

			if (!block.getType().name().endsWith("_WOOL"))
			{
				continue;
			}

			if (offset > highestDist)
			{
				highestDist = offset;
			}

			_wool.add(block);
		}
		Collections.shuffle(_wool);

		_captureDist = Math.pow(highestDist * (double) MAX_RADIUS + 0.5D, 2);
	}

	public void update()
	{
		// Store the number of players in a team in this map
		Map<GameTeam, Integer> playersOnPoint = new HashMap<>();

		for (GameTeam team : _host.GetTeamList())
		{
			// Populate
			playersOnPoint.put(team, 0);
			int players = 0;

			for (Player player : team.GetPlayers(true))
			{
				// Ignore for spectators
				// If they are not in the range
				if (UtilPlayer.isSpectator(player) || !isOnPoint(player.getLocation()))
				{
					continue;
				}

				// Increment
				players++;
			}

			// Put in map
			playersOnPoint.put(team, players);
		}

		// For each team get the team with the non-zero players
		GameTeam highest = null;
		int highestPlayers = 0;
		for (Entry<GameTeam, Integer> entry : playersOnPoint.entrySet())
		{
			GameTeam team = entry.getKey();
			int players = entry.getValue();

			// Only care if people are on it
			if (players > 0)
			{
				// If this is the first team on the point
				if (highest == null)
				{
					highest = team;
					highestPlayers = players;
				}
				// This means there are 2 teams on the point
				else
				{
					return;
				}
			}
		}

		// No one at all is on the point
		if (highest == null)
		{
			if (_owner == null)
			{
				return;
			}

			// If the owner isn't null, move the point's progress back
			highest = _owner;
			highestPlayers = 1;
		}
		// Players on the point
		// Only inform if it has been a while
		else if ((_owner == null || !_owner.equals(highest)) && UtilTime.elapsed(_lastInform, MIN_INFORM_TIME))
		{
			_lastInform = System.currentTimeMillis();

			String message = F.main("Game", "Team " + highest.GetFormattedName() + C.mBody + " is capturing the " + _colour + _name + C.mBody + " Beacon!");

			sendMessage(highest, message);

			if (_owner != null)
			{
				sendMessage(_owner, message);
			}
		}

		// If it has just reached the maximum progress, set the owner.
		if (_owner != null && _owner.equals(highest) && _progress >= (_owner == null ? MAX_PROGRESS_NEUTRAL : MAX_PROGRESS))
		{
			return;
		}

		capture(highest, highestPlayers);
	}

	private void sendMessage(GameTeam team, String message)
	{
		team.GetPlayers(true).forEach(player ->
		{
			player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SCREAM, 1f, 1f);
			player.sendMessage(message);
		});
	}

	private void capture(GameTeam team, int progress)
	{
		// No player has ever stood on the point
		if (_side == null)
		{
			_side = team;
		}

		// If it is the same team
		if (_side.equals(team))
		{
			// Increase progress
			_progress += progress;
			display(team, progress, true);

			// Captured
			if (_progress >= (_owner == null ? MAX_PROGRESS_NEUTRAL : MAX_PROGRESS))
			{
				_progress = MAX_PROGRESS;
				setOwner(team);
			}
		}
		// Other team
		else
		{
			// Point back to a neutral state
			if (_progress <= 0)
			{
				setBeaconColour(null);
				_side = team;
				_progress = 0;
				// Recursively call this method now that the first (same team) condition will be true
				capture(team, progress);
				return;
			}

			_progress -= progress;
			display(team, progress, false);
		}
	}

	private void setOwner(GameTeam team)
	{
		setBeaconColour(team);

		// Same team no need to inform
		if (_owner != null && _owner.equals(team))
		{
			return;
		}
		else
		{
			// As the point is easier to capture after the initial capture
			// We need to adjust the current progress, otherwise it has to go
			// from 10 to 0 then to 5 which is unintended
			_progress = MAX_PROGRESS;
		}

		String message = F.main("Game", "Team " + team.GetFormattedName() + C.mBody + " captured the " + _colour + _name + C.mBody + " Beacon!");

		if (_owner != null)
		{
			sendMessage(_owner, message);
		}
		sendMessage(team, message);

		_owner = team;

		UtilFirework.playFirework(_center, Type.BURST, team.GetColorBase(), false, false);
		org.bukkit.Bukkit.getPluginManager().callEvent(new CapturePointCaptureEvent(this));
	}

	private void display(GameTeam team, int progress, boolean forward)
	{
		double toChange = Math.ceil(_wool.size() / (double) (_owner == null ? MAX_PROGRESS_NEUTRAL : MAX_PROGRESS)) * progress + 1;
		int changed = 0;
		for (Block block : _wool)
		{
			if (changed >= toChange)
			{
				return;
			}

			Block glass = block.getRelative(BlockFace.UP);

			if (forward)
			{
				if (_changed.contains(block))
				{
					continue;
				}

				block.setType(getWoolMaterial(team));
				glass.setType(getGlassMaterial(team));
				changed++;
				_changed.add(block);
			}
			else
			{
				if (!_changed.contains(block))
				{
					continue;
				}

				block.setType(Material.WHITE_WOOL);
				glass.setType(Material.WHITE_STAINED_GLASS);
				changed++;
				_changed.remove(block);
			}

			glass.getWorld().playEffect(glass.getLocation().add(0.5, 0.5, 0.5), Effect.STEP_SOUND, block.getType());
		}
	}

	private void setBeaconColour(GameTeam team)
	{
		Block blockBelow = _center.getBlock().getRelative(BlockFace.DOWN);
		if (blockBelow.getType().name().contains("GLASS"))
		{
			blockBelow.setType(getGlassMaterial(team));
		}
		else
		{
			blockBelow.setType(getWoolMaterial(team));
		}
	}

	private Material getGlassMaterial(GameTeam team)
	{
		if (team == null) return Material.WHITE_STAINED_GLASS;
		return getGlassMaterial(team.GetColor());
	}

	private Material getGlassMaterial(ChatColor color)
	{
		switch (color)
		{
			case WHITE: return Material.WHITE_STAINED_GLASS;
			case GOLD: return Material.ORANGE_STAINED_GLASS;
			case LIGHT_PURPLE: return Material.PINK_STAINED_GLASS;
			case AQUA: return Material.LIGHT_BLUE_STAINED_GLASS;
			case YELLOW: return Material.YELLOW_STAINED_GLASS;
			case GREEN: return Material.LIME_STAINED_GLASS;
			case DARK_GRAY: return Material.GRAY_STAINED_GLASS;
			case GRAY: return Material.LIGHT_GRAY_STAINED_GLASS;
			case DARK_AQUA: return Material.CYAN_STAINED_GLASS;
			case DARK_PURPLE: return Material.PURPLE_STAINED_GLASS;
			case BLUE: return Material.BLUE_STAINED_GLASS;
			case DARK_BLUE: return Material.BLUE_STAINED_GLASS;
			case DARK_GREEN: return Material.GREEN_STAINED_GLASS;
			case RED: return Material.RED_STAINED_GLASS;
			case DARK_RED: return Material.RED_STAINED_GLASS;
			default: return Material.WHITE_STAINED_GLASS;
		}
	}

	private Material getWoolMaterial(GameTeam team)
	{
		if (team == null) return Material.WHITE_WOOL;
		return getWoolMaterial(team.GetColor());
	}

	private Material getWoolMaterial(ChatColor color)
	{
		switch (color)
		{
			case WHITE: return Material.WHITE_WOOL;
			case GOLD: return Material.ORANGE_WOOL;
			case LIGHT_PURPLE: return Material.PINK_WOOL;
			case AQUA: return Material.LIGHT_BLUE_WOOL;
			case YELLOW: return Material.YELLOW_WOOL;
			case GREEN: return Material.LIME_WOOL;
			case DARK_GRAY: return Material.GRAY_WOOL;
			case GRAY: return Material.LIGHT_GRAY_WOOL;
			case DARK_AQUA: return Material.CYAN_WOOL;
			case DARK_PURPLE: return Material.PURPLE_WOOL;
			case BLUE: return Material.BLUE_WOOL;
			case DARK_BLUE: return Material.BLUE_WOOL;
			case DARK_GREEN: return Material.GREEN_WOOL;
			case RED: return Material.RED_WOOL;
			case DARK_RED: return Material.RED_WOOL;
			default: return Material.WHITE_WOOL;
		}
	}

	public boolean isOnPoint(Location location)
	{
		return UtilMath.offsetSquared(_center, location) < _captureDist;
	}

	public String getName()
	{
		return _name;
	}

	public ChatColor getColour()
	{
		return _colour;
	}

	public GameTeam getOwner()
	{
		return _owner;
	}

	public Location getCenter()
	{
		return _center.clone();
	}
}
