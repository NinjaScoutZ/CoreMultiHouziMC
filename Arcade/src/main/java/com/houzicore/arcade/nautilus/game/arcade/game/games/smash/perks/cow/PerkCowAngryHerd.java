package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.cow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Cow;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilItem;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.damage.CustomDamageEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkCowAngryHerd extends SmashPerk
{

	private int _cooldownNormal;
	private int _cooldownSmash;
	private int _maxTime;
	private int _stuckTime;
	private int _forceMove;
	private float _hitBoxRadius;
	private int _hitFrequency;
	private int _damage;

	private List<DataCowCharge> _active = new ArrayList<>();

	public PerkCowAngryHerd()
	{
		super("Angry Herd", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Axe to use " + C.cGreen + "Angry Herd" });
	}

	@Override
	public void setupValues()
	{
		_cooldownNormal = getPerkTime("Cooldown Normal");
		_cooldownSmash = getPerkTime("Cooldown Smash");
		_maxTime = getPerkInt("Max Time (ms)");
		_stuckTime = getPerkInt("Stuck Time (ms)");
		_forceMove = getPerkInt("Force Move (ms)");
		_hitBoxRadius = getPerkFloat("Hit Box Radius");
		_hitFrequency = getPerkInt("Hit Frequency (ms)");
		_damage = getPerkInt("Damage");
	}

	@EventHandler
	public void shoot(PlayerInteractEvent event)
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

		if (!Recharge.Instance.use(player, GetName(), isSuperActive(player) ? _cooldownSmash : _cooldownNormal, true, true))
		{
			return;
		}

		event.setCancelled(true);

		for (double i = -2; i < 3; i++)
		{
			Vector dir = player.getLocation().getDirection();
			dir.setY(0);
			dir.normalize();

			Location loc = player.getLocation();

			loc.add(dir);
			loc.add(UtilAlg.getLeft(dir).multiply(i * 1.5));

			Manager.GetGame().CreatureAllowOverride = true;
			Class clazz;
			if (isSuperActive(player))
			{
				clazz = MushroomCow.class;
			}
			else
			{
				clazz = Cow.class;
			}
			Cow cow = (Cow) player.getWorld().spawn(loc, clazz);
			Manager.GetGame().CreatureAllowOverride = false;

			_active.add(new DataCowCharge(player, cow));
		}

		// Sound
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_COW_AMBIENT, 2f, 0.6f);

		// Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		Iterator<DataCowCharge> activeIter = _active.iterator();

		while (activeIter.hasNext())
		{
			DataCowCharge data = activeIter.next();

			// Expire
			if (UtilTime.elapsed(data.Time, _maxTime))
			{
				if (data.Cow.isValid())
				{
					data.Cow.remove();
					UtilParticle.PlayParticleToAll(ParticleType.EXPLODE, data.Cow.getLocation().add(0, 1, 0), 0f, 0f, 0f, 0f, 1, ViewDist.NORMAL);
				}

				activeIter.remove();
				continue;
			}

			// Set Moved
			if (UtilMath.offset(data.Cow.getLocation(), data.LastLoc) > 1)
			{
				data.LastLoc = data.Cow.getLocation();
				data.LastMoveTime = System.currentTimeMillis();
			}

			// Stuck Remove
			if (UtilTime.elapsed(data.LastMoveTime, _stuckTime))
			{
				if (data.Cow.isValid())
				{
					data.Cow.remove();
					UtilParticle.PlayParticleToAll(ParticleType.EXPLODE, data.Cow.getLocation().add(0, 1, 0), 0f, 0f, 0f, 0f, 1, ViewDist.NORMAL);
				}

				activeIter.remove();
				continue;
			}

			// Gravity
			if (UtilEnt.isGrounded(data.Cow))
			{
				data.Direction.setY(-0.1);
			}
			else
			{
				data.Direction.setY(Math.max(-1, data.Direction.getY() - 0.03));
			}

			// Move
			if (UtilTime.elapsed(data.LastMoveTime, _forceMove) && UtilEnt.isGrounded(data.Cow))
			{
				data.Cow.setVelocity(data.Direction.clone().add(new Vector(0, 0.75, 0)));
			}
			else
			{
				data.Cow.setVelocity(data.Direction);
			}

			if (Math.random() > 0.99)
			{
				data.Cow.getWorld().playSound(data.Cow.getLocation(), Sound.ENTITY_COW_AMBIENT, 1f, 1f);
			}

			if (Math.random() > 0.97)
			{
				data.Cow.getWorld().playSound(data.Cow.getLocation(), Sound.ENTITY_COW_STEP, 1f, 1.2f);
			}
			
			// Hit
			for (Player player : Manager.GetGame().GetPlayers(true))
			{
				if (player.equals(data.Player))
				{
					continue;
				}

				if (UtilMath.offset(player, data.Cow) < _hitBoxRadius)
				{
					if (Recharge.Instance.use(player, "Hit by " + data.Player.getName(), _hitFrequency, false, false))
					{
						// Damage Event
						Manager.GetDamage().NewDamageEvent(player, data.Player, null, DamageCause.CUSTOM, _damage, true, true, false, data.Player.getName(), GetName());

						UtilParticle.PlayParticleToAll(ParticleType.LARGE_EXPLODE, data.Cow.getLocation().add(0, 1, 0), 1f, 1f, 1f, 0, 12, ViewDist.LONG);

						data.Cow.getWorld().playSound(data.Cow.getLocation(), Sound.BLOCK_WOOD_BREAK, 0.75f, 0.8f);
						data.Cow.getWorld().playSound(data.Cow.getLocation(), Sound.ENTITY_COW_HURT, 1.5f, 0.75f);
					}
				}
			}
		}
	}

	@EventHandler
	public void Knockback(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
		{
			return;
		}
		
		event.AddKnockback(GetName(), 1.25);
	}
}
