package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.shared.common.util.C;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkKnockbackAttack extends Perk
{
	private double _power;
	
	public PerkKnockbackAttack(double power) 
	{
		super("Melee Knockback", new String[] 
				{
				C.cGray + "Melee attacks deal " + (int)(power*100) + "% Knockback.",
				});
		
		_power = power;
	}
		
	@EventHandler(priority = EventPriority.HIGH)
	public void Knockback(EntityDamageByEntityEvent event)
	{
		if (event.getCause() != DamageCause.ENTITY_ATTACK)
			return;

		if (!(event.getDamager() instanceof Player)) return;
		Player damager = ((Player) event.getDamager());
		if (damager == null)	return;

		if (!Kit.HasKit(damager))
			return;
		
		if (!Manager.IsAlive(damager))
			return;

  // /* event.AddKnockback(...) */;
	}
}
