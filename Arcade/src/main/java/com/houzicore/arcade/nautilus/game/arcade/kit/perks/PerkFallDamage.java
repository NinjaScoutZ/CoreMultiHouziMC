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

public class PerkFallDamage extends Perk
{
	private int _mod;
	
	public PerkFallDamage(int mod) 
	{
		super("Feather Falling", new String[] 
				{ 
				C.cGray + "You take " + mod + " damage from falling",
				});
		
		_mod = mod;
	}
		
	@EventHandler(priority = EventPriority.HIGH)
	public void DamageDecrease(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (event.getCause() != DamageCause.FALL)
			return;
		
		if (!(event.getEntity() instanceof Player)) return;
		Player damagee = ((Player) event.getEntity());
		if (damagee == null)	return;
		
		if (!Kit.HasKit(damagee))
			return;
		
		int decrease = 0;
		if (_mod < 0)
		{
			decrease = (int) -Math.min(Math.abs(_mod), event.getDamage());
		}
		
  // /* event.AddMod(...) */, GetName(), decrease, false);
	}
}
