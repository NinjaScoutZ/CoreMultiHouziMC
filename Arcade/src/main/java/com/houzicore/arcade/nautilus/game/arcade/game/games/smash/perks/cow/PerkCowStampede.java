package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.cow;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.damage.CustomDamageEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkCowStampede extends SmashPerk
{

	private long _speedTime;
	private double _stopSprintDamage;

	private Map<UUID, Long> _sprintTime = new HashMap<>();
	private Map<UUID, Integer> _sprintStr = new HashMap<>();

	public PerkCowStampede()
	{
		super("Stampede", new String[] { C.cGray + "Build up Speed Levels as you sprint.", C.cGray + "+1 damage for each Speed Level.", });
	}

	@Override
	public void setupValues()
	{
		_speedTime = getPerkTime("Speed Time");
		_stopSprintDamage = getPerkDouble("Stop Sprint Damage");
	}

	@Override
	public void unregisteredEvents()
	{
		_sprintStr.clear();
		_sprintTime.clear();
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
		{
			return;
		}

		for (Player cur : UtilServer.getPlayers())
		{
			if (!hasPerk(cur))
			{
				continue;
			}

			UUID key = cur.getUniqueId();

			// Active - Check for Disable
			if (_sprintTime.containsKey(key))
			{
				// Stopped
				if (!cur.isSprinting() || cur.getLocation().getBlock().isLiquid())
				{
					_sprintTime.remove(key);
					_sprintStr.remove(key);
					cur.removePotionEffect(PotionEffectType.SPEED);
					continue;
				}

				long time = _sprintTime.get(key);
				int str = _sprintStr.get(key);

				// Apply Speed
				if (str > 0)
				{
					Manager.GetCondition().Factory().Speed(GetName(), cur, cur, 1.9, !isSuperActive(cur) ? str - 1 : str, false, true, true);
				}

				// Upgrade Speed
				if (!UtilTime.elapsed(time, _speedTime))
				{
					continue;
				}

				_sprintTime.put(key, System.currentTimeMillis());

				if (str < 3)
				{
					_sprintStr.put(key, str + 1);

					// Effect
					cur.getWorld().playSound(cur.getLocation(), Sound.ENTITY_COW_HURT, 2f, 0.75f + 0.25f * str);
				}
			}
			else if (cur.isSprinting() || !cur.getLocation().getBlock().isLiquid())
			{
				// Start Timer
				if (!_sprintTime.containsKey(key))
				{
					_sprintTime.put(key, System.currentTimeMillis());
					_sprintStr.put(key, 0);
				}
			}
		}
	}

	@EventHandler
	public void particle(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (UUID key : _sprintStr.keySet())
		{
			if (_sprintStr.get(key) <= 0)
			{
				continue;
			}

			Player player = UtilPlayer.searchExact(key);

			if (player == null)
			{
				continue;
			}

			UtilParticle.PlayParticle(isSuperActive(player) ? ParticleType.RED_DUST : ParticleType.CRIT, player.getLocation(), (float) (Math.random() - 0.5), 0.2f + (float) Math.random(),
					(float) (Math.random() - 0.5), 0, _sprintStr.get(key) * 2, ViewDist.NORMAL, UtilServer.getPlayers());
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void damage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
		{
			return;
		}

		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
		{
			return;
		}

		Player damager = event.GetDamagerPlayer(false);
		
		if (damager == null)
		{
			return;
		}
		
		UUID key = damager.getUniqueId();

		if (!_sprintStr.containsKey(key))
		{
			return;
		}

		if (_sprintStr.get(key) == 0)
		{
			return;
		}

		LivingEntity damagee = event.GetDamageeEntity();

		if (damagee == null)
		{
			return;
		}

		// Remove
		_sprintTime.remove(key);
		int str = _sprintStr.remove(key);
		damager.removePotionEffect(PotionEffectType.SPEED);

		// Damage
		event.AddMod(damager.getName(), GetName(), str, true);
		event.AddKnockback(GetName(), 1 + (0.1 * str));

		// Inform
		UtilPlayer.message(damagee, F.main("Game", F.name(damager.getName()) + " hit you with " + F.skill(GetName()) + "."));

		// Effect
		damager.getWorld().playSound(damager.getLocation(), Sound.BLOCK_WOOD_BREAK, 1f, 2f);
		damager.getWorld().playSound(damager.getLocation(), Sound.ENTITY_COW_HURT, 2f, 2f);
	}

	@EventHandler
	public void damageCancel(CustomDamageEvent event)
	{
		if (event.IsCancelled())
		{
			return;
		}
		
		Player damagee = event.GetDamageePlayer();
		
		if (damagee == null || event.GetDamage() < _stopSprintDamage)
		{
			return;
		}
		
		clean(damagee);
		Manager.GetCondition().EndCondition(damagee, null, GetName());
	}

	@EventHandler
	public void quit(PlayerQuitEvent event)
	{
		clean(event.getPlayer());
	}

	public void clean(Player player)
	{
		_sprintTime.remove(player.getUniqueId());
		_sprintStr.remove(player.getUniqueId());
	}
}
