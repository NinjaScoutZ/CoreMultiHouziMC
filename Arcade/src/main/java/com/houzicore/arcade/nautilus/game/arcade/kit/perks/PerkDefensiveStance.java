package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.HashMap;

import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilGear;
import com.houzicore.shared.common.util.UtilTime;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkDefensiveStance extends Perk
{
	private HashMap<Player, Long> _useTime = new HashMap<Player, Long>();
	
	public PerkDefensiveStance() 
	{
		super("Defensive Stance", new String[] 
				{ 
				C.cYellow + "Block" + C.cGray + " with Sword to use " + C.cGreen + "Defensive Stance"
				});
	}
	
	@EventHandler
	public void skill(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
				
		if (!UtilGear.isSword(event.getPlayer().getItemInHand()))
			return;
		
		Player player = event.getPlayer();
		
		if (!Kit.HasKit(player))
			return;

		_useTime.put(player, System.currentTimeMillis());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void Damagee(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled())
			return;

		if (event.getCause() != DamageCause.ENTITY_ATTACK && event.getCause() != DamageCause.PROJECTILE)
			return;

		if (!(event.getEntity() instanceof Player)) return;
		Player damagee = ((Player) event.getEntity());
		if (damagee == null)	
			return;

		if (!_useTime.containsKey(damagee))
			return;
		
		if (!UtilTime.elapsed(_useTime.get(damagee), 500))
			return;
		
		if (!damagee.isBlocking())
			return;
	
		LivingEntity damager = (LivingEntity)event.getDamager();
		if (damager == null)
			return;

		Vector look = damagee.getLocation().getDirection();
		look.setY(0);
		look.normalize();

		Vector from = UtilAlg.getTrajectory(damagee, damager);
		from.normalize();

		//Not Infront
		if (damagee.getLocation().getDirection().subtract(from).length() > 1.4)
			// return;

		//Damage
		event.setCancelled(true);

		//Effect
		damagee.getWorld().playSound(damagee.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1f, 2f);
	}
}
