package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.houzicore.shared.common.util.C;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkIronSkin extends Perk
{
	private double _reduction;
	
	public PerkIronSkin(double d) 
	{
		super("Iron Skin", new String[] 
				{ 
				C.cGray + "You take " + d + " less damage from attacks",
				});
		
		_reduction = d;
	}
		
	@EventHandler(priority = EventPriority.HIGH)
	public void DamageDecrease(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (event.getCause() == DamageCause.FIRE_TICK)
			return;
		
		if (event.getDamage() <= 1)
			return;
		
		if (!(event.getEntity() instanceof Player)) return;
		Player damagee = ((Player) event.getEntity());
		if (damagee == null)	return;
		
		if (!Kit.HasKit(damagee))
			return;
		
  // /* event.AddMod(...) */, GetName(), -_reduction, false);
	}
}
