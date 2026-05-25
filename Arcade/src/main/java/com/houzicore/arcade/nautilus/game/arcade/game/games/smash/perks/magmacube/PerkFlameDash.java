package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.magmacube;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilItem;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.damage.CustomDamageEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashPerk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.data.FireflyData;

public class PerkFlameDash extends SmashPerk
{
	
	private int _cooldown;
	private int _time;
	private int _damageRadius;
	private int _knockbackMagnitude;
	
	private Set<FireflyData> _data = new HashSet<>();

	public PerkFlameDash()
	{
		super("Flame Dash", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Spade to use " + C.cGreen + "Flame Dash" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_time = getPerkInt("Time (ms)");
		_damageRadius = getPerkInt("Damage Radius");
		_knockbackMagnitude = getPerkInt("Knockback Magnitude");
	}

	@EventHandler
	public void Skill(PlayerInteractEvent event)
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

		if (!player.getInventory().getItemInMainHand().getType().name().endsWith("_SHOVEL") || !Recharge.Instance.use(player, GetName() + " Double Activation", 100, false, false))
		{
			return;
		}

		if (!hasPerk(player))
		{
			return;
		}

		if (!Recharge.Instance.usable(player, GetName()))
		{
			boolean done = false;
			for (FireflyData data : _data)
			{
				if (data.Player.equals(player))
				{
					data.Time = 0;
					done = true;
				}
			}

			if (done)
			{
				UtilPlayer.message(player, F.main("Skill", "You ended " + F.skill(GetName()) + "."));
				UpdateMovement();
			}
			else
			{
				Recharge.Instance.use(player, GetName(), _cooldown, true, true);
			}

			return;
		}

		for (Player other : UtilServer.getPlayers())
		{
			if (other.getSpectatorTarget() != null && other.getSpectatorTarget().equals(player))
			{
				other.setSpectatorTarget(null);
			}
		}

		Recharge.Instance.recharge(player, GetName());
		Recharge.Instance.use(player, GetName(), _cooldown, true, true);

		_data.add(new FireflyData(player));

		Manager.GetCondition().Factory().Cloak(GetName(), player, player, 2.5, false, false);

		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		UpdateMovement();
	}

	private void UpdateMovement()
	{
		Iterator<FireflyData> dataIterator = _data.iterator();

		while (dataIterator.hasNext())
		{
			FireflyData data = dataIterator.next();

			// Move
			if (!UtilTime.elapsed(data.Time, _time))
			{
				Vector vel = data.Location.getDirection();
				vel.setY(0);
				vel.normalize();
				vel.setY(0.05);

				data.Player.setVelocity(vel);

				// Sound
				data.Player.getWorld().playSound(data.Player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.6f, 1.2f);

				// Particles
				UtilParticle.PlayParticle(ParticleType.FLAME, data.Player.getLocation().add(0, 0.4, 0), 0.2f, 0.2f, 0.2f, 0f, 3, ViewDist.LONGER, UtilServer.getPlayers());
			}
			// End
			else
			{
				for (Player other : UtilPlayer.getNearby(data.Player.getLocation(), _damageRadius))
				{
					if (other.equals(data.Player))
					{
						continue;
					}
					
					if (UtilPlayer.isSpectator(other))
					{
						continue;
					}
					
					double dist = UtilMath.offset(data.Player.getLocation(), data.Location) / 2;

					// Damage Event
					Manager.GetDamage().NewDamageEvent(other, data.Player, null, DamageCause.CUSTOM, 2 + dist, true, true, false, data.Player.getName(), GetName());

					UtilPlayer.message(other, F.main("Game", F.elem(Manager.GetColor(data.Player) + data.Player.getName()) + " hit you with " + F.elem(GetName()) + "."));
				}

				// End Invisible
				Manager.GetCondition().EndCondition(data.Player, null, GetName());

				// Sound
				data.Player.getWorld().playSound(data.Player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1.2f);

				// Particles
				UtilParticle.PlayParticle(ParticleType.FLAME, data.Player.getLocation(), 0.1f, 0.1f, 0.1f, 0.3f, 100, ViewDist.MAX, UtilServer.getPlayers());
				UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, data.Player.getLocation().add(0, 0.4, 0), 0.2f, 0.2f, 0.2f, 0f, 1, ViewDist.MAX, UtilServer.getPlayers());

				dataIterator.remove();
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
		
		event.AddKnockback(GetName(), _knockbackMagnitude);
	}
}
