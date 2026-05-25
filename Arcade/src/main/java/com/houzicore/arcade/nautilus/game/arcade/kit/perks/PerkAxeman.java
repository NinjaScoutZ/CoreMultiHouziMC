package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.shared.common.util.C;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkAxeman extends Perk
{
	public PerkAxeman() 
	{
		super("Axe Master", new String[] 
				{
				C.cGray + "Deals +1 Damage with Axes",
				});
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void AxeDamage(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (!(event.getDamager() instanceof Player)) return;
		Player damager = (Player) event.getDamager();

		if (damager.getItemInHand() == null)
			return;
		
		if (!damager.getItemInHand().getType().toString().contains("_AXE"))
			return;
		
		if (!Kit.HasKit(damager))
			return;
		
  // /* event.AddMod(...) */, GetName(), 1, false);
	}
}
