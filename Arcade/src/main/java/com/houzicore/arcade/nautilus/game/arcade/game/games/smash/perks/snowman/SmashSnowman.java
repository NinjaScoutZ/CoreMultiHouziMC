package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.snowman;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilText;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.damage.CustomDamageEvent;

import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.TeamSuperSmash;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashUltimate;

public class SmashSnowman extends SmashUltimate
{

	private static final int COOLDOWN = 1000;

	private int _duration;
	private int _turretHealth;
	private int _rate;
	private float _knockbackMagnitude;

	private Map<Projectile, Player> _snowball = new HashMap<>();

	private Map<Snowman, Player> _turret = new HashMap<>();

	public SmashSnowman()
	{
		super("Snow Turret", new String[] {}, Sound.BLOCK_SNOW_STEP, 0);
	}

	@Override
	public void setupValues()
	{
		_duration = getPerkTime("Duration");
		_turretHealth = getPerkInt("Turret Health");
		_rate = getPerkInt("Rate");
		_knockbackMagnitude = getPerkFloat("Knockback Magnitude");
	}

	@Override
	public void activate(Player player)
	{
		player.sendMessage(F.main("Game", "Activated " + F.skill(GetName()) + "."));
		UtilPlayer.health(player, 3.5);

		Game game = Manager.GetGame();

		game.CreatureAllowOverride = true;
		Snowman ent = player.getWorld().spawn(player.getEyeLocation(), Snowman.class);
		game.CreatureAllowOverride = false;

		UtilEnt.Vegetate(ent);
		UtilEnt.ghost(ent, true, false);

		ent.getAttribute(Attribute.MAX_HEALTH).setBaseValue(_turretHealth);
		ent.setHealth(_turretHealth);

		UtilAction.velocity(ent, player.getLocation().getDirection(), 1, false, 0, 0.2, 1, false);
		
		Recharge.Instance.useForce(player, "Smash Cooldown - " + GetName(), COOLDOWN);

		_turret.put(ent, player);
	}

	@Override
	public void cancel(Player player)
	{
		if (Kit.HasKit(player))
		{
			player.sendMessage(F.main("Game", "Deactivated " + F.skill(GetName()) + "."));
		}
	}

	@EventHandler
	public void updateSnowman(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
		{
			return;
		}

		Iterator<Snowman> turretIter = _turret.keySet().iterator();

		while (turretIter.hasNext())
		{
			Snowman snowman = turretIter.next();
			
			if(snowman.getTicksLived() >= (_duration /50.0))
			{
				UtilParticle.PlayParticle(ParticleType.SNOWBALL_POOF, snowman.getLocation().add(0, 1, 0), 0.4f, 0.4f, 0.4f, 0, 12, ViewDist.LONG, UtilServer.getPlayers());
				snowman.remove();
				turretIter.remove();
				continue;
			}
			else
			{
				double amount = 1 - snowman.getTicksLived() / (_duration /50.0);
				snowman.setCustomName(UtilText.progressBar(amount, 10, '▌', '▌'));
				snowman.setCustomNameVisible(true);
			}
			
			Player player = _turret.get(snowman);
			Player target = UtilPlayer.getClosest(snowman.getLocation(), TeamSuperSmash.getTeam(Manager, player, true));

			if (target == null)
			{
				continue;
			}

			snowman.setTarget(target);

			// Snowball
			double mult = 1 + Math.min(3, UtilMath.offset(snowman, target) / 16);
			double heightBonus = UtilMath.offset(snowman, target) / 140;
			Vector rand = new Vector((Math.random() - 0.5) * 0.2, (Math.random() - 0.5) * 0.2, (Math.random() - 0.5) * 0.2);

			_snowball.put(snowman.launchProjectile(Snowball.class, UtilAlg.getTrajectory(snowman.getLocation(), target.getLocation()).multiply(mult).add(rand).add(new Vector(0, heightBonus, 0))),
					player);

			// Look dir
			UtilEnt.CreatureMoveFast(snowman, target.getLocation(), 0.1f);

			// Sound
			snowman.getWorld().playSound(snowman.getLocation(), Sound.BLOCK_SNOW_STEP, 0.6f, 1f);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void snowballHit(CustomDamageEvent event)
	{
		if (event.GetCause() != DamageCause.PROJECTILE)
		{
			return;
		}

		Projectile proj = event.GetProjectile();

		if (proj == null)
		{
			return;
		}

		if (!(proj instanceof Snowball))
		{
			return;
		}
		
		Player damager = _snowball.get(proj);

		if (damager == null)
		{
			return;
		}

		LivingEntity damagee = event.GetDamageeEntity();

		if (damagee == null)
		{
			return;
		}

		event.SetCancelled("Turret");

		if(TeamSuperSmash.getTeam(Manager, damager, true).contains(damagee))
		{
			return;
		}

		damagee.setVelocity(proj.getVelocity().multiply(0.3).add(new Vector(0, 0.3, 0)));

		if (!Recharge.Instance.use((Player) damagee, GetName() + " Hit", _rate, false, false))
		{
			return;
		}

		Manager.GetDamage().NewDamageEvent(damagee, damager, null, DamageCause.PROJECTILE, 2, false, true, false, UtilEnt.getName(_snowball.get(proj)), GetName());
	}

	@EventHandler
	public void damageCancel(CustomDamageEvent event)
	{
		if (_turret.containsKey(event.GetDamageeEntity()))
		{
			event.SetCancelled("Turret Immunity");
		}
	}

	@EventHandler
	public void clean(ProjectileHitEvent event)
	{
		_snowball.remove(event.getEntity());
	}

	@EventHandler
	public void knockback(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
		{
			return;
		}

		event.AddKnockback(GetName(), _knockbackMagnitude);
	}
	
	@Override
	public boolean isUsable(Player player)
	{
		return Recharge.Instance.usable(player, "Smash Cooldown - " + GetName());
	}
	
}
