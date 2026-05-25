package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.magmacube;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilItem;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkMagmaBlast extends SmashPerk
{
	
	private int _cooldown;
	private float _velocity;
	private int _fireTicks;
	private int _velocityRadius;
	private int _fireRadius;
	private int _damage;
	
	private final Map<LargeFireball, Location> _proj = new HashMap<>();

	public PerkMagmaBlast()
	{
		super("Magma Blast", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Axe to use " + C.cGreen + "Magma Blast" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_velocity = getPerkFloat("Velocity");
		_fireTicks =  getPerkInt("Fire Ticks");
		_velocityRadius = getPerkInt("Velocity Radius");
		_fireRadius = getPerkInt("Fire Radius");
		_damage = getPerkInt("Damage");
	}

	@EventHandler
	public void Shoot(PlayerInteractEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		if (UtilBlock.usable(event.getClickedBlock()))
		{
			return;
		}

		Player player = event.getPlayer();

		if (!player.getInventory().getItemInMainHand().getType().name().endsWith("_AXE"))
		{
			return;
		}

		if (!hasPerk(player))
		{
			return;
		}

		if (!Recharge.Instance.use(player, GetName(), _cooldown, true, true))
		{
			return;
		}
		
		event.setCancelled(true);

		// Action
		LargeFireball ball = player.launchProjectile(LargeFireball.class);
		ball.setShooter(player);
		ball.setIsIncendiary(false);
		ball.setYield(0);
		ball.teleport(player.getEyeLocation().add(player.getLocation().getDirection().multiply(1)));

		Vector dir = player.getLocation().getDirection().multiply(_velocity);
		ball.setDirection(dir);

		// Knockback
		UtilAction.velocity(player, player.getLocation().getDirection().multiply(-1), 1.2, false, 0, 0.2, 1.2, true);

		// Add
		_proj.put(ball, player.getLocation());

		// Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));

		// Effect
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CREEPER_DEATH, 2f, 1.5f);
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		Iterator<LargeFireball> projIterator = _proj.keySet().iterator();

		while (projIterator.hasNext())
		{
			LargeFireball proj = projIterator.next();

			if (!proj.isValid())
			{
				projIterator.remove();
				proj.remove();
			}
		}
	}

	@EventHandler
	public void Collide(ProjectileHitEvent event)
	{
		Projectile proj = event.getEntity();

		if (!_proj.containsKey(proj))
		{
			return;
		}
		
		if (proj.getShooter() == null)
		{
			return;
		}
		
		if (!(proj.getShooter() instanceof Player))
		{
			return;
		}

		proj.remove();

		Player shooter = (Player) proj.getShooter();
		// Velocity Players
		Map<Player, Double> hitMap = UtilPlayer.getInRadius(proj.getLocation().subtract(0, 1, 0), _velocityRadius);

		hitMap.forEach((player, range) ->
		{
			if (range > 0.8)
			{
				range = 1D;
			}

			Manager.GetDamage().NewDamageEvent(player, shooter, proj, DamageCause.PROJECTILE, range * _damage, false, true, false, shooter.getName(), GetName());

			// Velocity
			UtilAction.velocity(player, UtilAlg.getTrajectory(proj.getLocation().add(0, -0.5, 0), player.getEyeLocation()), 1 + 2 * range, false, 0, 0.2 + 0.4 * range, 1.2, true);
		});

		// Particles
		UtilParticle.PlayParticleToAll(ParticleType.LAVA, proj.getLocation(), 0.1f, 0.1f, 0.1f, 0.1f, 50, ViewDist.LONG);
	}
}
