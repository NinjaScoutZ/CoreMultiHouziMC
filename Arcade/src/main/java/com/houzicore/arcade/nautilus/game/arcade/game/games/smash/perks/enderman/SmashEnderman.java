package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.enderman;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilFirework;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.damage.CustomDamageEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashUltimate;

public class SmashEnderman extends SmashUltimate
{
	
	private int _dragonVelocity;
	private int _hitCooldown;
	private int _damageRadius;
	private int _damage;
	private int _knockbackMagnitude;
	
	private Map<UUID, EnderDragon> _dragons = new HashMap<>();

	public SmashEnderman()
	{
		super("Ender Dragon", new String[] {}, Sound.ENTITY_ENDER_DRAGON_GROWL, 0);
	}

	@Override
	public void setupValues()
	{
		super.setupValues();

		_dragonVelocity = getPerkInt("Dragon Velocity");
		_hitCooldown = getPerkTime("Hit Cooldown");
		_damageRadius = getPerkInt("Damage Radius");
		_damage = getPerkInt("Damage");
		_knockbackMagnitude = getPerkInt("Knockback Magnitude");
	}

	@Override
	public void activate(Player player)
	{
		super.activate(player);
		
		Manager.GetGame().CreatureAllowOverride = true;
		EnderDragon dragon = player.getWorld().spawn(player.getLocation().add(0, 5, 0), EnderDragon.class);
		dragon.setAI(false);
		Manager.GetGame().CreatureAllowOverride = false;

		dragon.setCustomName(C.cYellow + player.getName() + "'s Dragon");

		UtilFirework.playFirework(dragon.getLocation(), Type.BALL_LARGE, Color.BLACK, true, true);

		_dragons.put(player.getUniqueId(), dragon);
	}

	@Override
	public void cancel(Player player)
	{
		super.cancel(player);
		
		EnderDragon dragon = _dragons.remove(player.getUniqueId());
		
		if (dragon == null)
		{
			return;
		}
		
		player.leaveVehicle();
		dragon.remove();
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (UUID key : _dragons.keySet())
		{
			Player player = UtilPlayer.searchExact(key);
			
			if (player == null)
			{
				continue;
			}
			
			EnderDragon dragon = _dragons.get(key);

			// Mount
			if (dragon.getPassenger() == null || !dragon.getPassenger().equals(player))
			{
				player.leaveVehicle();
				dragon.setPassenger(player);
			}

			// Move
			dragon.setVelocity(player.getLocation().getDirection().multiply(0.8));
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void explosionBlocks(EntityExplodeEvent event)
	{
		event.blockList().clear();
	}

	@EventHandler(priority = EventPriority.LOW)
	public void suffocationCancel(CustomDamageEvent event)
	{
		if (event.GetCause() != DamageCause.SUFFOCATION)
		{
			return;
		}
		
		if (event.GetDamageePlayer() == null)
		{
			return;
		}
			
		if (isUsingUltimate(event.GetDamageePlayer()))
		{
			event.SetCancelled("Enderman Dragon Suffocate");
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void dragonDamagerCancel(CustomDamageEvent event)
	{
		if (event.GetDamagerEntity(false) == null)
		{
			return;
		}
		
		if (!_dragons.values().contains(event.GetDamagerEntity(false)))
		{
			return;
		}
		
		event.SetCancelled("Dragon Damage Cancel");
	}

	@EventHandler
	public void updateDamageAoe(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		for (UUID key : _dragons.keySet())
		{
			Player player = UtilPlayer.searchExact(key);
			
			if (player == null)
			{
				continue;
			}
			
			EnderDragon dragon = _dragons.get(key);

			UtilParticle.PlayParticleToAll(ParticleType.FIREWORKS_SPARK, dragon.getLocation(), 0, 0, 0, 0, 1, ViewDist.LONGER);

			for (Player other : Manager.GetGame().GetPlayers(true))
			{
				if (other.equals(player))
				{
					continue;
				}
				
				if (UtilMath.offset(dragon.getLocation().add(0, 4, 0), other.getLocation()) < _damageRadius && Recharge.Instance.use(other, "Hit By Dragon", _hitCooldown, false, false))
				{
					// Damage Event
					Manager.GetDamage().NewDamageEvent(other, player, null, DamageCause.CUSTOM, _damage, true, true, false, player.getName(), GetName());
				}
			}
		}
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

	@EventHandler(priority = EventPriority.LOW)
	public void voidCancel(CustomDamageEvent event)
	{
		if (event.GetCause() != DamageCause.VOID)
		{
			return;
		}
		
		Player player = event.GetDamageePlayer();
		
		if (player == null)
		{
			return;
		}
		
		if (!isUsingUltimate(player))
		{
			return;
		}
		
		event.SetCancelled("Dragon Void Immunity");
	}
}
