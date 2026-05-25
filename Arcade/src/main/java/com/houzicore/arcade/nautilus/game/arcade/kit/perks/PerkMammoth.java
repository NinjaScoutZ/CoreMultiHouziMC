package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.shared.common.util.C;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkMammoth extends Perk
{
	public PerkMammoth() 
	{
		super("Mammoth", new String[] 
				{
				C.cGray + "Take 50% knockback and deal 150% knockback",
				});
	}
		
	@EventHandler(priority = EventPriority.HIGH)
	public void KnockbackIncrease(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (!(event.getDamager() instanceof Player)) return;
		Player damager = (Player) event.getDamager();
		
		if (!Kit.HasKit(damager))
			return;
		
  // /* event.AddKnockback(...) */, 1.5d);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void KnockbackDecrease(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (!(event.getEntity() instanceof Player)) return;
		Player damagee = (Player) event.getEntity();
		
		if (!Kit.HasKit(damagee))
			return;
		
  // /* event.AddKnockback(...) */, 0.5d);
	}
}
