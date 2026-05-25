package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.shared.common.util.C;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkKnockbackArrow extends Perk
{
	private double _power;
	
	public PerkKnockbackArrow()
	{
		this(1.5);
	}

	public PerkKnockbackArrow(double power) 
	{
		super("Arrow Knockback", new String[] 
				{
				C.cGray + "Arrows deal " + (int)(power*100) + "% Knockback.",
				});
		
		_power = power;
	}
		
	@EventHandler(priority = EventPriority.HIGH)
	public void Knockback(EntityDamageByEntityEvent event)
	{
		if (!(event.getDamager() instanceof Arrow))
			return;
			
		Arrow arrow = (Arrow) event.getDamager();
		if (!(arrow.getShooter() instanceof Player))
			return;
			
		Player shooter = (Player) arrow.getShooter();
		if (!Kit.HasKit(shooter))
			return;
			
		if (!(event.getEntity() instanceof Player))
			return;
			
		Player hit = (Player) event.getEntity();
		if (!Manager.GetGame().IsAlive(hit))
			return;
			
		// Apply custom extra knockback
		org.bukkit.util.Vector knockback = hit.getLocation().toVector().subtract(shooter.getLocation().toVector()).normalize();
		knockback.multiply(_power).setY(0.4);
		
		hit.setVelocity(knockback);
	}
}
