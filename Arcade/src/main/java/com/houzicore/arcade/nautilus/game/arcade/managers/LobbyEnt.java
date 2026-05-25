package com.houzicore.arcade.nautilus.game.arcade.managers;

import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class LobbyEnt 
{
	private Kit _kit;
	private GameTeam _team;
	private Entity _ent;
	private Location _loc;
	private List<ArmorStand> _holograms = new ArrayList<>();
	private List<Entity> _extraEntities = new ArrayList<>();

	public LobbyEnt(Entity ent, Location loc, Kit kit)
	{
		_ent = ent;
		_loc = loc;
		_kit = kit;
	}
	
	public LobbyEnt(Entity ent, Location loc, GameTeam team)
	{
		_ent = ent;
		_loc = loc;
		_team = team;
	}
	
	public Kit GetKit()
	{
		return _kit;
	}
	
	public GameTeam GetTeam()
	{
		return _team;
	}
	
	public Entity GetEnt()
	{
		return _ent;
	}
	
	public Location GetLocation()
	{
		return _loc;
	}
	
	public List<ArmorStand> GetHolograms()
	{
		return _holograms;
	}

	public List<Entity> GetExtraEntities()
	{
		return _extraEntities;
	}
	
	public void AddHologram(ArmorStand stand)
	{
		_holograms.add(stand);
	}

	public void AddExtraEntity(Entity entity)
	{
		if (entity != null)
			_extraEntities.add(entity);
	}
	
	public void RemoveHolograms()
	{
		for (ArmorStand stand : _holograms)
		{
			if (stand != null)
				stand.remove();
		}
		_holograms.clear();
	}

	public void RemoveExtraEntities()
	{
		for (Entity entity : _extraEntities)
		{
			if (entity != null)
				entity.remove();
		}
		_extraEntities.clear();
	}

	private boolean _isPlayerNear = false;
	private double _baseScale = 1.0;

	public boolean IsPlayerNear()
	{
		return _isPlayerNear;
	}

	public void SetPlayerNear(boolean near)
	{
		_isPlayerNear = near;
	}

	public double GetBaseScale()
	{
		return _baseScale;
	}

	public void SetBaseScale(double scale)
	{
		_baseScale = scale;
	}
}
