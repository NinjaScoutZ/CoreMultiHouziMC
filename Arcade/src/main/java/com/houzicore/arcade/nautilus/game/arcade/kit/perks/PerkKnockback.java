package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import org.bukkit.EntityEffect;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkKnockback extends Perk
{
	private double _power;
	
	public PerkKnockback(double power) 
	{
		super("Knockback", new String[] 
				{
				C.cGray + "Attacks gives knockback with " + power + " power.",
				});
		
		_power = power;
	}
		
	@EventHandler(priority = EventPriority.HIGH)
	public void Knockback(EntityDamageByEntityEvent event)
	{
		if (!(event.getDamager() instanceof Player)) return;
		Player damager = ((Player) event.getDamager());
		if (damager == null)	return;
		
		if (!Kit.HasKit(damager))
			return;
		
		if (!Manager.IsAlive(damager))
			return;
				
  // /* event.SetKnockback(false) */;
		
		if (!Recharge.Instance.use(damager, "KB " + UtilEnt.getName(event.getEntity()), 400, false, false))
			return;
		
		event.getEntity().playEffect(EntityEffect.HURT);
		
		UtilAction.velocity(event.getEntity(), 
				UtilAlg.getTrajectory(damager, event.getEntity()), 
				_power, false, 0, 0.1, 10, true);
	}
}
