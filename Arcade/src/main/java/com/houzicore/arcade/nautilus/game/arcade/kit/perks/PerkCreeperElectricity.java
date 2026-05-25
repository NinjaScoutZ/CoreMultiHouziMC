package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.houzicore.shared.api.disguise.DisguiseRequest;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkCreeperElectricity extends Perk
{
	private HashMap<Player, Long> _active = new HashMap<Player, Long>();
	
	public PerkCreeperElectricity() 
	{
		super("Lightning Shield", new String[] 
				{
				"When hit by a non-melee attack, you gain " + C.cGreen + "Lightning Shield"
				});
	}


	@EventHandler
	public void Shield(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (event.getCause() == DamageCause.ENTITY_ATTACK)
			return;
		
		if (event.getCause() == DamageCause.FIRE_TICK)
			return;
		
		if (!(event.getEntity() instanceof Player)) return;
		Player damagee = ((Player) event.getEntity());
		if (damagee == null)		return;
		
		if (!Kit.HasKit(damagee))
			return;
		
		_active.put(damagee, System.currentTimeMillis());
		
		SetPowered(damagee, true);
		
		//Sound
		damagee.getWorld().playSound(damagee.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 3f, 1.25f);
		
		//Inform
		UtilPlayer.message(damagee, F.main("Skill", "You gained " + F.skill(GetName()) + "."));
	}

	
	@EventHandler
	public void Update(UpdateEvent event)  
	{
		if (event.getType() != UpdateType.TICK)
			return;

		Iterator<Player> shieldIterator = _active.keySet().iterator();

		while (shieldIterator.hasNext())
		{
			Player player = shieldIterator.next();

			if (!IsPowered(player))
			{
				shieldIterator.remove();
				SetPowered(player, false);
				continue;
			}
			
			if (UtilTime.elapsed(_active.get(player), 2000))
			{
				shieldIterator.remove();

				SetPowered(player, false);
				
				//Sound
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 3f, 0.75f);
			}
		}
	}

	@EventHandler
	public void Damage(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (event.getCause() != DamageCause.ENTITY_ATTACK)
			return;
		
		if (!(event.getEntity() instanceof Player)) return;
		Player damagee = ((Player) event.getEntity());
		if (damagee == null)		return;
		
		if (!Kit.HasKit(damagee))
			return;
		
		if (!IsPowered(damagee))
			return;
		
		event.setCancelled(true);
		
		//Inform
		UtilPlayer.message(damagee, F.main("Skill", "You hit " + F.elem(UtilEnt.getName(((Player) event.getDamager()))) + " with " + F.skill(GetName()) + "."));
		
		//Lightning
		damagee.getWorld().strikeLightningEffect(damagee.getLocation());
		SetPowered(damagee, false);
		
		if (!(event.getDamager() instanceof org.bukkit.entity.LivingEntity)) return;
		
		//Damage Event
		Manager.GetDamage().NewDamageEvent((org.bukkit.entity.LivingEntity)event.getDamager(), damagee, null,
				DamageCause.LIGHTNING, 4, true, true, false,
				damagee.getName(), GetName());
	}
	
	public void SetPowered(Player player, boolean powered)
	{
		Manager.GetDisguise().getService().getActiveSession(player.getUniqueId())
				.map(session -> session.request())
				.filter(request -> "CREEPER".equalsIgnoreCase(request.variantKey()))
				.filter(request -> !String.valueOf(powered).equals(request.attributes().get("creeperPowered")))
				.map(request -> request.withAttribute("creeperPowered", String.valueOf(powered)))
				.ifPresent(request -> applyUpdatedDisguise(player, request));
	}

	private void applyUpdatedDisguise(Player player, DisguiseRequest request)
	{
		Manager.GetDisguise().getService().apply(player, request);
	}
	
	public boolean IsPowered(Player player)
	{
		return _active.containsKey(player);
	}

	@EventHandler
	public void Knockback(EntityDamageByEntityEvent event)
	{
		if (event.getCause().name() == null || !event.getCause().name().contains(GetName()))
			return;
		
  // /* event.AddKnockback(...) */, 2.5);
	}
}
