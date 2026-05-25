package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.HashMap;
import java.util.WeakHashMap;

import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.kit.SmashPerk;

public class PerkSpidersNest extends SmashPerk
{
	private WeakHashMap<LivingEntity, Double> _preHealth = new WeakHashMap<LivingEntity, Double>();
	
	public PerkSpidersNest() 
	{
		super("Spider Nest", new String[] {});
	}
	
	@Override
	public void addSuperCustom(Player player)
	{
		//Nest
		HashMap<Block, Double> blocks = UtilBlock.getInRadius(player.getLocation().getBlock(), 16);
		
		for (Block block : blocks.keySet())
		{
			if (blocks.get(block) > 0.07)
				continue;
			
			if (!UtilBlock.airFoliage(block))
				continue;
			
			if (block.getY() > player.getLocation().getY() + 10)
				continue;
			
			if (block.getY() < player.getLocation().getY() - 10)
				continue;
			
			Manager.GetBlockRestore().Add(block, 30, (byte)0, (long) (30000 + 5000 * Math.random()));	
		}
		
		//Regen
		Manager.GetCondition().Factory().Regen(GetName(), player, player, 30, 0, false, false, false);
	}
		
	@EventHandler(priority = EventPriority.HIGH)
	public void damagePre(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (event.getCause() != DamageCause.ENTITY_ATTACK &&
			event.getCause() != DamageCause.PROJECTILE &&
			event.getCause() != DamageCause.CUSTOM)
			return;
		
		if (!(event.getDamager() instanceof Player)) return;
		Player damager = ((Player) event.getDamager());
		if (damager == null)
			return;
		
		LivingEntity damagee = (LivingEntity)event.getEntity();
		if (damagee == null)
			// return;
		
		if (!isSuperActive(damager))
			return;
		
		_preHealth.put(damagee, damagee.getHealth());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void damagePost(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (!(event.getDamager() instanceof Player)) return;
		Player damager = ((Player) event.getDamager());
		if (damager == null)
			return;
		
		LivingEntity damagee = (LivingEntity)event.getEntity();
		if (damagee == null)
			// return;
				
		if (!isSuperActive(damager))
			return;
		
		if (!_preHealth.containsKey(damagee))
			return;

		double diff = (_preHealth.remove(damagee) - damagee.getHealth())/2d;
		
		if (diff <= 0)
			// return;
		
		damager.setMaxHealth(Math.min(60, damager.getMaxHealth() + diff));
		damager.setHealth(damager.getHealth() + diff);
		
		UtilParticle.PlayParticle(ParticleType.HEART, damager.getLocation().add(0, 1, 0), 0, 0, 0, 0, 1,
				ViewDist.LONG, UtilServer.getPlayers());
		
		UtilParticle.PlayParticle(ParticleType.RED_DUST, damagee.getLocation().add(0, 1, 0), 0.4f, 0.4f, 0.4f, 0, 12,
				ViewDist.LONG, UtilServer.getPlayers());
		
		if (event.getCause() == DamageCause.ENTITY_ATTACK)
			damager.getWorld().playSound(damager.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 1.5f, 1f);		
	}
}
