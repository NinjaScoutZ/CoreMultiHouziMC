package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.shared.common.util.C;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkDamageSnow extends Perk
{
	private int _damage;
	private double _knockback;
	
	public PerkDamageSnow()
	{
		this(1, 1.6);
	}

	public PerkDamageSnow(int damage, double knockback) 
	{
		super("Snow Attack", new String[] 
				{
				C.cGray + "+" + damage + " Damage and " + (int)((knockback-1)*100) + "% Knockback to enemies on snow.",
				});
		
		_damage = damage;
		_knockback = knockback;
	}
		
	@EventHandler(priority = EventPriority.HIGH)
	public void Knockback(EntityDamageByEntityEvent event)
	{
		if (event.getEntity().getLocation().getBlock().getType() != org.bukkit.Material.SNOW)
			return;
		
		if (!(event.getDamager() instanceof Player)) return;
		Player damager = ((Player) event.getDamager());
		if (damager == null)	return;
				
		if (!Kit.HasKit(damager))
			return;
		
		if (!Manager.IsAlive(damager))
			return;
		
  // /* event.AddMod(...) */, GetName(), _damage, false);
  // /* event.AddKnockback(...) */;
	}
}
