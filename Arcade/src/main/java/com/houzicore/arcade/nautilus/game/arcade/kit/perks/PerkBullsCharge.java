package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffectType;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilGear;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
//import com.houzicore.shared.condition.Condition.ConditionType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkBullsCharge extends Perk
{
	public PerkBullsCharge() 
	{
		super("Bulls Charge", new String[] 
				{ 
				C.cYellow + "Right-Click" + C.cGray + " with Axe to use " + C.cGreen + "Bulls Charge"
				});
	}
	
	@EventHandler
	public void skill(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		
		if (UtilBlock.usable(event.getClickedBlock()))
			return;
		
		if (!UtilGear.isAxe(event.getPlayer().getItemInHand()))
			return;
		
		Player player = event.getPlayer();
		
		if (!Kit.HasKit(player))
			return;
		
		if (!Recharge.Instance.use(player, GetName(), 12000, true, true))
			return;
		
		//Action
//		Manager.GetCondition().Factory().Speed(GetName(), player, player, 6, 1, false, true, true);

		//Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));

		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 1.5f, 0f);
		player.getWorld().playEffect(player.getLocation(), Effect.STEP_SOUND, 49);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void damage(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (event.getCause() != DamageCause.ENTITY_ATTACK)
			return;

		if (!(event.getDamager() instanceof Player)) return;
		Player damager = ((Player) event.getDamager());
		LivingEntity damagee = (LivingEntity)event.getEntity();

		if (damager == null || damagee == null)
			// return;
		
		//Isn't using Bulls Charge
//		if (!Manager.GetCondition().HasCondition(damager, ConditionType.SPEED, GetName()))
			return;

		//Condition
//		Manager.GetCondition().Factory().Slow(GetName(), damagee, damager, 4, 1, false, true, true, true);
//		Manager.GetCondition().EndCondition(damager, ConditionType.SPEED, GetName());

		//Damage
  // /* event.SetKnockback(false) */;
		
		//Effect
		damager.getWorld().playSound(damager.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 1.5f, 0f);
		damager.getWorld().playSound(damager.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1.5f, 0.5f);

		//Inform
		UtilPlayer.message(damagee, F.main("Game", F.name(damager.getName()) + " hit you with " + F.skill(GetName()) + "."));
		UtilPlayer.message(damager, F.main("Game", "You hit " + F.name(UtilEnt.getName(damagee)) +" with " + F.skill(GetName()) + "."));
	}
	
	@EventHandler
	public void particle(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (Manager.GetGame() == null)
			return;
		
		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!Kit.HasKit(player))
				continue;
			
			if (player.hasPotionEffect(PotionEffectType.SPEED))
				UtilParticle.PlayParticle(ParticleType.CRIT, player.getLocation(), 
					(float)(Math.random() - 0.5), 0.2f + (float)(Math.random() * 1), (float)(Math.random() - 0.5), 0, 2,
					ViewDist.LONG, UtilServer.getPlayers());
		}
	}
}
