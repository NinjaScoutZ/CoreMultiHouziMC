package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.houzicore.shared.common.util.C;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkStrength extends Perk
{
	private int _power;
	
	public PerkStrength(int power) 
	{
		super("Strength", new String[] 
				{ 
				C.cGray + "You deal " + power + " more damage",
				});
		
		_power = power;
	}
		
	@EventHandler(priority = EventPriority.HIGH)
	public void DamageDecrease(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (!(event.getDamager() instanceof Player)) return;
		Player damager = ((Player) event.getDamager());
		if (damager == null)	return;
		
		if (!Kit.HasKit(damager))
			return;
		
  // /* event.AddMod(...) */, GetName(), _power, false);
	}
}
