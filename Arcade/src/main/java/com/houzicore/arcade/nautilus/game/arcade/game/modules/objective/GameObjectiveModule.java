package com.houzicore.arcade.nautilus.game.arcade.game.modules.objective;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;

import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.modules.GameModule;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.updater.UpdateType;

public class GameObjectiveModule extends GameModule
{
	private List<GameObjective> _objectives = new ArrayList<>();
	
	public GameObjectiveModule(Game game)
	{
		super(game);
	}
	
	public void registerObjective(GameObjective obj)
	{
		_objectives.add(obj);
		if (_game.IsLive())
		{
			obj.setupHologram();
		}
	}
	
	public List<GameObjective> getObjectives()
	{
		return _objectives;
	}
	
	@EventHandler
	public void onGameStateChange(com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent event)
	{
		if (event.GetState() == com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState.Live)
		{
			for (GameObjective obj : _objectives)
			{
				obj.setupHologram();
			}
		}
	}
	
	@Override
	public void unregister()
	{
		for (GameObjective obj : _objectives)
		{
			obj.cleanUp();
		}
		_objectives.clear();
		super.unregister();
	}
	
	@EventHandler
	public void onTick(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
			
		if (!_game.IsLive())
			return;
			
		for (GameObjective obj : _objectives)
		{
			obj.onTick();
		}
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event)
	{
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK)
			return;
			
		if (event.getClickedBlock() == null)
			return;
			
		for (GameObjective obj : _objectives)
		{
			// Check if click is on the exact block or extremely close
			if (event.getClickedBlock().getLocation().distance(obj.getLocation().getBlock().getLocation()) < 0.5)
			{
				obj.onInteract(event.getPlayer());
				event.setCancelled(true);
			}
		}
	}
}
