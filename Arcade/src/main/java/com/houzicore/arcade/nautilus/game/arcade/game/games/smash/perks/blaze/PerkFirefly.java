package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.blaze;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilItem;
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
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.data.FireflyData;

public class PerkFirefly extends SmashPerk
{

	private int _cooldown;
	private int _duration ;
	private int _damage;

	private int _radiusNormal;
	private float _velocityNormal;

	private int _radiusSmash;
	private float _velocitySmash;

	private int _hitFrequency;
	private int _warmupTime;
	private int _minCancelDamage;
	private int _knockbackMagnitude;

	private Set<FireflyData> _data = new HashSet<>();

	private int _tick = 0;

	public PerkFirefly()
	{
		super("Firefly", new String[]{C.cYellow + "Right-Click" + C.cGray + " with Axe to use " + C.cGreen + "Firefly"});
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_duration = getPerkInt("Duration (ms)");
		_damage = getPerkInt("Damage");
		_radiusNormal = getPerkInt("Radius Normal");
		_velocityNormal = getPerkFloat("Velocity Normal");
		_radiusSmash = getPerkInt("Radius Smash");
		_velocitySmash = getPerkFloat("Velocity Smash");
		_hitFrequency = getPerkInt("Hit Frequency (ms)");
		_warmupTime = getPerkInt("Warmup Time (ms)");
		_minCancelDamage = getPerkInt("Min Cancel Damage");
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

		if (!player.getInventory().getItemInMainHand().getType().name().endsWith("_AXE"))
		{
			return;
		}

		if (!hasPerk(player))
		{
			return;
		}

		if (isSuperActive(player))
		{
			return;
		}

		if (!Recharge.Instance.use(player, GetName(), _cooldown, true, true))
		{
			return;
		}

		activate(player, this);
	}

	/*
	 * It is split like this so Blaze's Smash can be called without the need to
	 * copy code.
	 */
	public void activate(Player player, Perk caller)
	{
		_data.add(new FireflyData(player));

		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(caller.GetName()) + "."));
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		_tick++;

		Iterator<FireflyData> dataIterator = _data.iterator();

		//There are a lot of magic numbers here, they are all arbitrary sound and particle values.

		while (dataIterator.hasNext())
		{
			FireflyData data = dataIterator.next();

			Player player = data.Player;
			boolean superActive = isSuperActive(data.Player);
			String skillName = superActive ? "Phoenix" : GetName();

			// Warmup
			if (!UtilTime.elapsed(data.Time, _warmupTime) && !superActive)
			{
				player.setVelocity(new Vector(0, 0, 0));
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.2f, 0.6f);
				data.Location = player.getLocation();

				// Sound and Effect
				UtilParticle.PlayParticleToAll(ParticleType.FIREWORKS_SPARK, player.getLocation().add(0, 1, 0), 0.6f, 0.6f, 0.6f, 0, 10, ViewDist.LONG);

				float progress = (float) (System.currentTimeMillis() - data.Time) / _warmupTime;

				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_BURN, 0.5f, 1f + progress);
			}
			// Velocity
			else if (!UtilTime.elapsed(data.Time, _duration) || superActive)
			{
				player.setVelocity(player.getLocation().getDirection().multiply(superActive ? _velocitySmash : _velocityNormal).add(new Vector(0, 0.15, 0)));
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.6f, 1.2f);

				// Sound and Effect
				if (!superActive)
				{
					UtilParticle.PlayParticleToAll(ParticleType.FLAME, player.getLocation().add(0, 1, 0), 0.6f, 0.6f, 0.6f, 0, 40, ViewDist.LONG);

					player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 1.25f);
				}
				else
				{
					UtilParticle.PlayParticleToAll(ParticleType.FLAME, player.getLocation().add(0, 1, 0), 1f, 1f, 1f, 0, 60, ViewDist.LONG);
					UtilParticle.PlayParticleToAll(ParticleType.LAVA, player.getLocation().add(0, 1, 0), 1f, 1f, 1f, 0, 40, ViewDist.LONG);

					player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.75f, 0.75f);
				}

				for (Player other : UtilPlayer.getNearby(player.getLocation(), superActive ? _radiusSmash : _radiusNormal))
				{
					if (other.equals(player))
					{
						continue;
					}

					if (UtilPlayer.isSpectator(other))
					{
						continue;
					}

					other.broadcastHurtAnimation(new java.util.ArrayList<>(org.bukkit.Bukkit.getOnlinePlayers()));

					if (_tick % 12 == 0)
					{
						if (Recharge.Instance.use(other, GetName() + " hit by " + player.getName(), _hitFrequency, false, false))
						{
							// Damage Event
							Manager.GetDamage().NewDamageEvent(other, player, null, DamageCause.CUSTOM, _damage, true, true, false, player.getName(), skillName);

							UtilPlayer.message(other, F.main("Game", F.elem(Manager.GetColor(player) + player.getName()) + " hit you with " + F.elem(skillName) + "."));
						}
					}
				}
			}
			else
			{
				dataIterator.remove();
			}
		}
	}

	@EventHandler
	public void FireflyDamage(CustomDamageEvent event)
	{
		if (event.GetDamage() < _minCancelDamage)
		{
			return;
		}

		if (!(event.GetDamagerEntity(true) instanceof Player))
		{
			return;
		}

		Iterator<FireflyData> dataIterator = _data.iterator();

		while (dataIterator.hasNext())
		{
			FireflyData data = dataIterator.next();

			if (!data.Player.equals(event.GetDamageeEntity()))
			{
				continue;
			}

			if (!UtilTime.elapsed(data.Time, _warmupTime) && !isSuperActive(data.Player))
			{
				if (isTeamDamage(data.Player, event.GetDamagerPlayer(true)))
				{
					continue;
				}

				dataIterator.remove();
			}
			else
			{
				event.SetCancelled("Firefly Immunity");
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

	@EventHandler
	public void playerDeath(PlayerDeathEvent event)
	{
		_data.removeIf(data -> data.Player.equals(event.getEntity()));
	}
}
