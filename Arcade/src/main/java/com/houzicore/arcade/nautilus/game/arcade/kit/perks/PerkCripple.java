package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.shared.common.util.C;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkCripple extends Perk
{
	private int _power;
	private double _time;
	
	public PerkCripple(int power, double time) 
	{
		super("Knockback", new String[] 
				{
				C.cGray + "Attacks give Slow " + power + " for " + time + " seconds.",
				});
		
		_power = power;
		_time = time;
	}
		
	@EventHandler(priority = EventPriority.HIGH)
	public void Knockback(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (event.getCause() != DamageCause.ENTITY_ATTACK)
			return;
		
		if (!(event.getDamager() instanceof Player)) return;
		Player damager = ((Player) event.getDamager());
		if (damager == null)	return;
		
		if (!Kit.HasKit(damager))
			return;
		
		if (!Manager.IsAlive(damager))
			return;
		
  // /* event.SetKnockback(false) */;
		
		if (!(event.getEntity() instanceof org.bukkit.entity.LivingEntity)) return;
		org.bukkit.entity.LivingEntity target = (org.bukkit.entity.LivingEntity) event.getEntity();
		Manager.GetCondition().Factory().Slow("Cripple", target, damager, _time, _power, false, false, false, false);
	}
}
