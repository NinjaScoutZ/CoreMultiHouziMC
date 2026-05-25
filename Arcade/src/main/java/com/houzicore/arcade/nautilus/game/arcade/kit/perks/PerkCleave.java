package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilGear;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilTime;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class PerkCleave extends Perk
{
	private boolean _axeOnly;
	
	public PerkCleave(double splash, boolean axeOnly) 
	{
		super("Cleave", new String[] 
				{ 
				C.cGray + "Attacks deal " + (int)(100*splash) + "% damage to nearby enemies",
				});
		
		_axeOnly = axeOnly;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void Skill(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled())
			return;

		if (event.getCause() != DamageCause.ENTITY_ATTACK)
			return;

		if (event.getCause().name() != null)
			return;
		
		//Dont allow usage in early game
		if (UtilTime.elapsed(Manager.GetGame().GetStateTime(), 30000))
			return;

		//Damager
		if (!(event.getDamager() instanceof Player)) return;
		Player damager = ((Player) event.getDamager());
		if (damager == null)	return;

		if (_axeOnly && !UtilGear.isAxe(damager.getItemInHand()))
			return;
		
		if (!UtilGear.isWeapon(damager.getItemInHand()))
			return;

		if (!Kit.HasKit(damager))
			return;

		//Damagee
		LivingEntity damagee = (LivingEntity)event.getEntity();
		if (damagee == null)	return;

		//Damage
  // /* event.AddMod(...) */, GetName(), 0, false);

		//Splash
		for (Player other : UtilPlayer.getNearby(damagee.getLocation(), 3))
		{
			if (other.equals(damagee))
				continue;
			
			if (other.equals(damager))
				continue;

			if (!Manager.canHurt(damager, other))
				continue;

			//Damage Event
			Manager.GetDamage().NewDamageEvent(other, damager, null,
					DamageCause.CUSTOM, event.getDamage(), true, true, false,
					damager.getName(), GetName());
		}
	}
}
