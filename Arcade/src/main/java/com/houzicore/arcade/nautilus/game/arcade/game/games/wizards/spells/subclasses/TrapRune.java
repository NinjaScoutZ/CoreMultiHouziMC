package com.houzicore.arcade.nautilus.game.arcade.game.games.wizards.spells.subclasses;

import java.util.ArrayList;

import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilShapes;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.core.explosion.Explosion;
import com.houzicore.arcade.nautilus.game.arcade.game.games.wizards.Wizards;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class TrapRune
{

	private Location _runeLocation;
	private float _runeSize;
	private Player _runeCaster;
	private int _ticksLived;
	private Wizards _wizards;
	private int _spellLevel;

	public boolean onRuneTick()
	{

		if (!_runeCaster.isOnline() || UtilPlayer.isSpectator(_runeCaster))
		{
			return true;
		}
		else if (_ticksLived++ > 2000)
		{
			return true;
		}
		else
		{
			if (_ticksLived <= 100)
			{
				if (_ticksLived % 15 == 0)
				{
					initialParticles();
				}

				if (_ticksLived == 100)
				{
					UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, _runeLocation, 0, _runeSize / 4, 0, _runeSize / 4,
							(int) (_runeSize * 10),
							ViewDist.LONG, UtilServer.getPlayers());
				}
			}
			else
			{
				if (!isValid())
				{
					trapCard();
					return true;
				}
				else
				{
					for (Player player : _wizards.GetPlayers(true))
					{
						if (isInTrap(player.getLocation()))
						{
							trapCard();
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public TrapRune(Wizards wizards, Player player, Location location, int spellLevel)
	{
		_wizards = wizards;
		_runeCaster = player;
		_runeLocation = location;
		_spellLevel = spellLevel;
		_runeSize = Math.max(1, spellLevel * 0.8F);
	}

	public void initialParticles()
	{
		for (Location loc : getBox(0.3))
		{
			for (double y = 0; y < 1; y += 0.2)
			{
				_runeLocation.getWorld().spawnParticle(org.bukkit.Particle.CAMPFIRE_COSY_SMOKE, loc, 1, 0, 0, 0, 0);
			}
		}
	}

	public ArrayList<Location> getBox(double spacing)
	{
		ArrayList<Location> boxLocs = getBoxCorners();
		ArrayList<Location> returns = new ArrayList<Location>();

		for (int i = 0; i < boxLocs.size(); i++)
		{

			int a = i + 1 >= boxLocs.size() ? 0 : i + 1;
			returns.addAll(UtilShapes.getLinesDistancedPoints(boxLocs.get(i), boxLocs.get(a), spacing));
			returns.add(boxLocs.get(i));

		}
		return returns;
	}

	public ArrayList<Location> getBoxCorners()
	{
		ArrayList<Location> boxPoints = new ArrayList<Location>();

		boxPoints.add(_runeLocation.clone().add(-_runeSize, 0, -_runeSize));
		boxPoints.add(_runeLocation.clone().add(_runeSize, 0, -_runeSize));
		boxPoints.add(_runeLocation.clone().add(_runeSize, 0, _runeSize));
		boxPoints.add(_runeLocation.clone().add(-_runeSize, 0, _runeSize));

		return boxPoints;
	}

	public boolean isInTrap(Location loc)
	{
		if (loc.getX() >= _runeLocation.getX() - _runeSize && loc.getX() <= _runeLocation.getX() + _runeSize)
		{
			if (loc.getZ() >= _runeLocation.getZ() - _runeSize && loc.getZ() <= _runeLocation.getZ() + _runeSize)
			{
				if (loc.getY() >= _runeLocation.getY() - 0.1 && loc.getY() <= _runeLocation.getY() + 0.9)
				{
					return true;
				}
			}
		}
		return false;
	}

	public boolean isValid()
	{
		return !UtilBlock.solid(_runeLocation.getBlock())
				|| UtilBlock.solid(_runeLocation.getBlock().getRelative(BlockFace.DOWN));
		/*
		for (double x = -RuneSize; x <= RuneSize; x++)
		{
		    for (double z = -RuneSize; z <= RuneSize; z++)
		    {

		        Block b = RuneLocation.clone().add(x, 0, z).getBlock();
		        if (UtilBlock.solid(b) || !UtilBlock.solid(b.getRelative(BlockFace.DOWN)))
		        {
		            return false;
		        }

		    }
		}*/
	}

	public void trapCard()
	{
		_runeLocation.getWorld().playSound(_runeLocation, Sound.ENTITY_WITHER_SHOOT, 5, (float) _runeSize * 2);

		double radius = (float) _runeSize * 1.2F;
		double maxDamage = (_spellLevel * 4) + 6;
		
		java.util.HashMap<org.bukkit.entity.LivingEntity, Double> targets = com.houzicore.shared.common.util.UtilEnt.getInRadius(_runeLocation.clone().add(0, 0.3, 0), radius);
		for (org.bukkit.entity.LivingEntity target : targets.keySet()) {
			_wizards.getArcadeManager().GetDamage().NewDamageEvent(target, _runeCaster, null, org.bukkit.event.entity.EntityDamageEvent.DamageCause.CUSTOM, maxDamage * targets.get(target), false, true, false, "Wizards", "Trap Rune");
		}
		
		java.util.HashSet<org.bukkit.block.Block> blocks = new java.util.HashSet<org.bukkit.block.Block>(com.houzicore.shared.common.util.UtilBlock.getInRadius(_runeLocation.clone().add(0, 0.3, 0), (float) _runeSize * 2F).keySet());
		_wizards.getArcadeManager().GetExplosion().BlockExplosion(blocks, _runeLocation.clone().add(0, 0.3, 0), false, false);

		for (Location loc : getBox(0.3))
		{
			for (double y = 0; y < 1; y += 0.2)
			{
				_runeLocation.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, loc, 1, 0, 0, 0, 0);
			}
		}
	}
}
