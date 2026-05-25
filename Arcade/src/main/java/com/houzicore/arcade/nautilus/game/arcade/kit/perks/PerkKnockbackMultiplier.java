package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.shared.common.util.C;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkKnockbackMultiplier extends Perk
{
	private double _power;
	
	public PerkKnockbackMultiplier(double power) 
	{
		super("Knockback", new String[] 
				{
				C.cGray + "You take " + (int)(power*100) + "% Knockback.",
				});
		
		_power = power;
	}
		
	@EventHandler(priority = EventPriority.HIGH)
	public void Knockback(EntityDamageByEntityEvent event)
	{
		if (!(event.getEntity() instanceof Player)) return;
		Player damagee = ((Player) event.getEntity());
		if (damagee == null)	return;
		
		if (!Kit.HasKit(damagee))
			return;
		
		if (!Manager.IsAlive(damagee))
			return;
		
  // /* event.AddKnockback(...) */;
	}
}
