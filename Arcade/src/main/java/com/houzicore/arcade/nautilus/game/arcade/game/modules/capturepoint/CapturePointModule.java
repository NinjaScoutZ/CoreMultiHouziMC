package com.houzicore.arcade.nautilus.game.arcade.game.modules.capturepoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.BeaconInventory;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.arcade.nautilus.game.arcade.game.modules.GameModule;

public class CapturePointModule extends GameModule<Game>
{

	private final List<CapturePoint> _capturePoints;

	public CapturePointModule()
	{
		super(null); // Will be set during register(Game) in parent
		_capturePoints = new ArrayList<>(3);
	}

	public CapturePointModule(Game game)
	{
		super(game);
		_capturePoints = new ArrayList<>(3);
	}

	public void register(Game game)
	{
		// Since parent GameModule uses final T _game, and we want compatibility with legacy instantiation:
		// new CapturePointModule(); followed by .register(this);
		// We set it via reflection or we can just implement register method
		try
		{
			java.lang.reflect.Field field = GameModule.class.getDeclaredField("_game");
			field.setAccessible(true);
			field.set(this, game);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		register();
	}

	@EventHandler
	public void prepare(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Prepare)
		{
			return;
		}

		for (Entry<String, Location> entry : getLocationStartsWith("POINT").entrySet())
		{
			String[] split = entry.getKey().split(" ");

			if (split.length < 3)
			{
				continue;
			}

			String name = split[1];
			ChatColor colour;

			try
			{
				colour = ChatColor.valueOf(split[2]);
			}
			catch (IllegalArgumentException e)
			{
				continue;
			}

			_capturePoints.add(new CapturePoint(getGame(), name, colour, entry.getValue()));
		}
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC || !getGame().IsLive())
		{
			return;
		}

		for (CapturePoint point : _capturePoints)
		{
			point.update();
		}
	}

	@EventHandler
	public void beaconInteract(InventoryOpenEvent event)
	{
		if (getGame().IsLive() && event.getInventory() instanceof BeaconInventory)
		{
			// event.setCancelled(true);
		}
	}

	public String getDisplayString()
	{
		StringBuilder out = new StringBuilder();

		for (CapturePoint point : _capturePoints)
		{
			out.append(point.getOwner() == null ? C.cWhite : point.getOwner().GetColor()).append(point.getName()).append(" ");
		}

		return out.toString().trim();
	}

	public boolean isOnPoint(Location location)
	{
		for (CapturePoint point : _capturePoints)
		{
			if (point.isOnPoint(location))
			{
				return true;
			}
		}

		return false;
	}

	public List<CapturePoint> getCapturePoints()
	{
		return _capturePoints;
	}

	private Map<String, Location> getLocationStartsWith(String s)
	{
		Map<String, Location> map = new HashMap<>();

		for (String key : getGame().WorldData.GetAllCustomLocs().keySet())
		{
			if (key.startsWith(s))
			{
				map.put(key, getGame().WorldData.GetCustomLocs(key).get(0));
			}
		}

		return map;
	}
}
