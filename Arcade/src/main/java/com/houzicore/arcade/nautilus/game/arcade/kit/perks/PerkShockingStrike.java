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

public class PerkShockingStrike extends Perk
{
	public PerkShockingStrike() 
	{
		super("Shocking Strikes", new String[] 
				{
				C.cGray + "Your attacks Shock/Blind/Slow opponents.",
				});
	}
		
	@EventHandler(priority = EventPriority.MONITOR)
	public void Effect(EntityDamageByEntityEvent event)
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
		
		if (!(event.getEntity() instanceof org.bukkit.entity.LivingEntity)) return;
		org.bukkit.entity.LivingEntity damagee = (org.bukkit.entity.LivingEntity)event.getEntity();
		
		Manager.GetCondition().Factory().Slow(GetName(), damagee, damager, 2, 1, false, false, false, false);
		Manager.GetCondition().Factory().Blind(GetName(), damagee, damager, 1, 0, false, false, false);
		Manager.GetCondition().Factory().Shock(GetName(), damagee, damager, 1, false, false);
	}
}
