package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.creeper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.core.disguise.disguises.DisguiseBase;
import com.houzicore.shared.core.disguise.disguises.DisguiseCreeper;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.damage.CustomDamageEvent;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkCreeperElectricity extends Perk
{
	
	private int _duration;
	private int _shock;
	private int _damage;
	private float _knockbackMagnitude;
	
	private final Map<UUID, Long> _active = new HashMap<>();

	public PerkCreeperElectricity()
	{
		super("Lightning Shield", new String[] { "When hit by a non-melee attack, you gain " + C.cGreen + "Lightning Shield" });
	}

	@Override
	public void setupValues()
	{
		_duration = getPerkTime("Duration");
		_shock = getPerkInt("Shock");
		_damage = getPerkInt("Damage");
		_knockbackMagnitude = getPerkFloat("Knockback Magnitude");
	}

	@EventHandler
	public void Shield(CustomDamageEvent event)
	{
		if (event.IsCancelled())
		{
			return;
		}

		if (event.GetCause() == DamageCause.ENTITY_ATTACK || event.GetCause() == DamageCause.FIRE_TICK || event.GetCause() == DamageCause.STARVATION || event.GetCause() == DamageCause.POISON)
		{
			return;
		}

		Player damagee = event.GetDamageePlayer();

		if (damagee == null)
		{
			return;
		}
		
		if (!hasPerk(damagee))
		{
			return;
		}
		
		_active.put(damagee.getUniqueId(), System.currentTimeMillis());

		SetPowered(damagee, true);

		Manager.GetCondition().Factory().Speed(GetName(), damagee, damagee, 4, 1, false, false, false);

		// Sound
		damagee.getWorld().playSound(damagee.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 3f, 1.25f);

		// Inform
		UtilPlayer.message(damagee, F.main("Skill", "You gained " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		Iterator<UUID> shieldIterator = _active.keySet().iterator();

		while (shieldIterator.hasNext())
		{
			UUID key = shieldIterator.next();
			Player player = UtilPlayer.searchExact(key);
			
			if (player == null)
			{
				shieldIterator.remove();
				continue;
			}

			if (!IsPowered(player))
			{
				shieldIterator.remove();
				SetPowered(player, false);
				continue;
			}

			if (UtilTime.elapsed(_active.get(key), _duration))
			{
				shieldIterator.remove();

				SetPowered(player, false);

				// Sound
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 3f, 0.75f);
			}
		}
	}

	@EventHandler
	public void Damage(CustomDamageEvent event)
	{
		if (event.IsCancelled())
		{
			return;
		}
		
		if (event.GetCause() != DamageCause.ENTITY_ATTACK)
		{
			return;
		}
		
		Player damagee = event.GetDamageePlayer();
		
		if (damagee == null)
		{
			return;
		}
		
		if (!hasPerk(damagee))
		{
			return;
		}
		
		if (!IsPowered(damagee))
		{
			return;
		}
		
		event.SetCancelled("Lightning Shield");

		// Inform
		UtilPlayer.message(damagee, F.main("Skill", "You hit " + F.elem(UtilEnt.getName(event.GetDamagerEntity(false))) + " with " + F.skill(GetName()) + "."));

		// Elec
		damagee.getWorld().strikeLightningEffect(damagee.getLocation());
		Manager.GetCondition().Factory().Shock(GetName(), event.GetDamagerEntity(false), event.GetDamageeEntity(), _shock, false, false);

		SetPowered(damagee, false);

		// Damage Event
		Manager.GetDamage().NewDamageEvent(event.GetDamagerEntity(false), damagee, null, DamageCause.LIGHTNING, _damage, true, true, false, damagee.getName(), GetName());
	}

	public DisguiseCreeper GetDisguise(Player player)
	{
		DisguiseBase disguise = Manager.GetDisguise().getDisguise(player);

		if (disguise == null)
		{
			return null;
		}

		if (!(disguise instanceof DisguiseCreeper))
		{
			return null;
		}

		return (DisguiseCreeper) disguise;
	}

	public void SetPowered(Player player, boolean powered)
	{
		DisguiseCreeper creeper = GetDisguise(player);
		
		if (creeper == null)
		{
			return;
		}
		
		creeper.SetPowered(powered);

		Manager.GetDisguise().updateDisguise(creeper);
	}

	public boolean IsPowered(Player player)
	{
		DisguiseCreeper creeper = GetDisguise(player);

		if (creeper == null)
		{
			return false;
		}

		return creeper.IsPowered();
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
