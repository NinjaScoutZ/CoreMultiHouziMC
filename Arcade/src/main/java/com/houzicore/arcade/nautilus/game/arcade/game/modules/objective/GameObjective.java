package com.houzicore.arcade.nautilus.game.arcade.game.modules.objective;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.shared.core.hologram.Hologram;

public abstract class GameObjective
{
	protected Game Host;
	protected String Name;
	protected Location EntityLocation;
	
	private Hologram _hologram;
	
	public GameObjective(Game host, String name, Location loc)
	{
		Host = host;
		Name = name;
		EntityLocation = loc;
	}
	
	public Location getLocation()
	{
		return EntityLocation;
	}
	
	public String getName()
	{
		return Name;
	}
	
	public void setupHologram()
	{
		if (_hologram != null)
			return;
			
		_hologram = new Hologram(Host.Manager.getHologramManager(), getHologramLocation(), getHologramText());
		_hologram.start();
	}
	
	public void updateHologram()
	{
		if (_hologram != null)
		{
			_hologram.setText(getHologramText());
		}
	}
	
	public void cleanupHologram()
	{
		if (_hologram != null)
		{
			_hologram.stop();
			_hologram = null;
		}
	}
	
	// Override these in subclasses
	
	protected abstract String[] getHologramText();
	
	protected Location getHologramLocation()
	{
		return EntityLocation.clone().add(0.5, 2.5, 0.5);
	}
	
	public void onTick()
	{
		// Optional logic
	}
	
	public void onInteract(Player player)
	{
		// Handle player right-click/interact
	}
	
	public void onDamage(EntityDamageEvent event)
	{
		// Handle damage
	}
	
	public void cleanUp()
	{
		cleanupHologram();
	}
}
