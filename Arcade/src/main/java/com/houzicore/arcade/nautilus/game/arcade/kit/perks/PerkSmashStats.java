package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class PerkSmashStats extends Perk
{
	private double _damage;
	private double _knockbackTaken;
	private double _regen;
	
	public PerkSmashStats(double damage, double knockbackTaken, double regen, double armor) 
	{
		super("Smash Stats", new String[] 
				{
				
				(C.cAqua + "Damage: " + C.cWhite + damage) + C.cWhite + "        " + (C.cAqua + "Knockback Taken: " + C.cWhite + (int)(knockbackTaken*100) + "%"),
				(C.cAqua + "Armor: " + C.cWhite + armor) + C.cWhite + "          " + (C.cAqua + "Health Regeneration: " + C.cWhite + regen + " per Second"),
				});
		
		_damage = damage;
		_knockbackTaken = knockbackTaken;
		_regen = regen;
	} 
	
	@EventHandler(priority = EventPriority.HIGH)
	public void Damage(EntityDamageByEntityEvent event)
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
		
		double mod = _damage - event.getDamage();
				
  // /* event.AddMod(...) */, "Attack", mod, true);
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
	
	@EventHandler
	public void Regeneration(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;
		
		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!Kit.HasKit(player))
				continue;
			
			UtilPlayer.health(player, _regen);
		}
	}
}
