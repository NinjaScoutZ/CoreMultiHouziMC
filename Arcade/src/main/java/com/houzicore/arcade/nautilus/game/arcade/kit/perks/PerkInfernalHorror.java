package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.HashSet;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.events.PlayerGameRespawnEvent;
import com.houzicore.arcade.nautilus.game.arcade.kit.SmashPerk;

public class PerkInfernalHorror extends SmashPerk
{
	public HashSet<Player> _active = new HashSet<Player>();

	public PerkInfernalHorror() 
	{
		super("Infernal Horror", new String[] 
				{ 
				C.cGray + "Tranform into " + F.skill("Infernal Horror") + " at 100% Rage.",
				C.cGray + "Charge your Rage by dealing/taking damage."
				});
	}
	
	@Override
	public void addSuperCustom(Player player)
	{
		_active.add(player);
		player.setExp(0.9999f);
	}

	@EventHandler
	public void energyUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!Kit.HasKit(player))
				continue;

			player.setExp((float) Math.max(0, player.getExp()-0.001));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void damagerEnergy(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled())
			return;

		if (event.getCause() == DamageCause.FIRE_TICK)
			return;
		
		if (!(event.getDamager() instanceof Player)) return;
		Player damager = ((Player) event.getDamager());
		if (damager == null)	return;

		if (!Kit.HasKit(damager))
			return;

		damager.setExp(Math.min(0.999f, damager.getExp() + (float)(event.getDamage()/60d)));
		
		activeCheck(damager);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void damageeEnergy(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled())
			return;

		if (event.getCause() == DamageCause.FIRE_TICK)
			return;
		
		if (event.getCause() == DamageCause.VOID)
			return;


		if (!(event.getEntity() instanceof Player)) return;
		Player damagee = ((Player) event.getEntity());
		if (damagee == null)	return;

		if (!Kit.HasKit(damagee))
			return;

		damagee.setExp(Math.min(0.999f, damagee.getExp() + (float)(event.getDamage()/60d)));
				
		activeCheck(damagee);
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void damageBoost(EntityDamageByEntityEvent event)
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
		
		if (!_active.contains(damager))
			return;
		
  // /* event.AddMod(...) */, GetName(), 1, false);
	}

	@EventHandler
	public void check(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : Manager.GetGame().GetPlayers(true))
			if (Kit.HasKit(player))
				activeCheck(player);
	}

	public void activeCheck(Player player)
	{
		//Active
		if (_active.contains(player))
		{
			if (!isSuperActive(player))
				player.setExp((float) Math.max(0, player.getExp()-0.005));
			
			if (player.getExp() > 0)
			{
				//Condition
				Manager.GetCondition().Factory().Speed(GetName(), player, player, 0.9, 1, false, false, false);
				
				//Particles
				UtilParticle.PlayParticle(ParticleType.FLAME, player.getLocation().add(0, 1, 0), 0.25f, 0.25f, 0.25f, 0, 1,
						ViewDist.LONG, UtilServer.getPlayers());
				
				if (Math.random() > 0.9)
					UtilParticle.PlayParticle(ParticleType.LAVA, player.getLocation().add(0, 1, 0), 0.25f, 0.25f, 0.25f, 0, 1,
							ViewDist.LONG, UtilServer.getPlayers());
			}
			else
			{
				_active.remove(player);
				
				//Inform
				UtilPlayer.message(player, F.main("Skill", "You are no longer " + F.skill("Infernal Horror") + "."));
			}
		}
		//Not Active
		else if (player.getExp() > 0.99)
		{
			_active.add(player);

			//Sound
			player.getWorld().playSound(player.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 2f, 1f);
			player.getWorld().playSound(player.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 2f, 1f);
			
			//Inform
			UtilPlayer.message(player, F.main("Skill", "You transformed into " + F.skill("Infernal Horror") + "."));
		}
	}

	@EventHandler
	public void clean(PlayerGameRespawnEvent event)
	{
		event.GetPlayer().setExp(0f);
		_active.remove(event.GetPlayer());
	}

	public boolean isActive(Player player) 
	{
		return _active.contains(player);
	}
}
