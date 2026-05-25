package com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.skeletalhorse;

import com.houzicore.shared.common.util.*;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.damage.CustomDamageEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.games.smash.perks.SmashPerk;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.util.*;

public class PerkDeadlyBones extends SmashPerk
{
	
	private int _rate;
	private int _ticks ;
	private int _damageRadius;
	private int _damage;
	private float _knockbackMagnitude;
	
	private Map<Item, UUID> _active = new HashMap<>();

	public PerkDeadlyBones()
	{
		super("Deadly Bones", new String[] { C.cGray + "Drop explosive bones when you take damage." });
	}

	@Override
	public void setupValues()
	{
		_rate = getPerkInt("Rate (ms)");
		_ticks = getPerkInt("Ticks");
		_damageRadius = getPerkInt("Damage Radius");
		_damage = getPerkInt("Damage");
		_knockbackMagnitude = getPerkFloat("Knockback Magnitude");
	}

	@EventHandler
	public void damageActivate(CustomDamageEvent event)
	{
		if (event.IsCancelled() || event.GetCause() == DamageCause.STARVATION)
		{
			return;
		}
		
		Player player = event.GetDamageePlayer();
		
		if (player == null)
		{
			return;
		}
		
		if (!hasPerk(player))
		{
			return;
		}
		
		if (!Recharge.Instance.use(player, GetName(), _rate, false, false))
		{
			return;
		}
		
		_active.put(player.getWorld().dropItemNaturally(player.getLocation().add(0, 0.5, 0), ItemStackFactory.Instance.CreateStack(Material.BONE, (byte) 0, 1, "Explosive Bone " + System
				.currentTimeMillis())), player.getUniqueId());
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		// Using copy to prevent concurrency issues for when an explosion
		// damages
		// another player with this perk active adding a new item to the list
		// while
		// we are still in the loop
		List<Item> itemListCopy = new ArrayList<>(_active.keySet());

		for (Item item : itemListCopy)
		{
			// Not Ready
			if (item.isValid() && item.getTicksLived() < _ticks)
			{
				continue;
			}
			
			// Effect
			UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, item.getLocation(), 0, 0, 0, 0, 1, ViewDist.MAX, UtilServer.getPlayers());
			item.getWorld().playSound(item.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.4f);

			UUID key = _active.get(item);
			Player player = UtilPlayer.searchExact(key);
			
			if (player == null)
			{
				item.remove();
				_active.remove(item);
				continue;
			}
			
			// Damage
			Map<LivingEntity, Double> targets = UtilEnt.getInRadius(item.getLocation(), _damageRadius);
			
			for (LivingEntity cur : targets.keySet())
			{
				if (cur.equals(player))
				{
					continue;
				}
				
				Manager.GetDamage().NewDamageEvent(cur, player, null, DamageCause.CUSTOM, _damage * targets.get(cur) + 1, true, true, false, player.getName(), GetName());
			}

			// Remove
			item.remove();
			_active.remove(item);
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
}
