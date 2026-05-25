package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Style;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.updater.UpdateType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkHorsePet extends Perk
{
	private HashMap<Player, Horse> _horseMap = new HashMap<Player, Horse>();
	private HashMap<Player, Long> _deathTime = new HashMap<Player, Long>();
	
	public PerkHorsePet() 
	{
		super("Horse Master", new String[] 
				{
				C.cGray + "You have a loyal horse companion.",
				});
	}

	@Override
	public void Apply(Player player) 
	{
		spawnHorse(player, false);
	}
	
	public void spawnHorse(Player player, boolean baby)
	{
		if (!Manager.GetGame().IsAlive(player))
			return;
		
		Manager.GetGame().CreatureAllowOverride = true;
		Horse horse = player.getWorld().spawn(player.getLocation(), Horse.class);
		Manager.GetGame().CreatureAllowOverride = false;

		horse.setAdult();
		horse.setAgeLock(true);
		horse.setColor(Color.BROWN);
		horse.setStyle(Style.NONE);
		horse.setOwner(player);
		horse.setMaxDomestication(1);
		horse.setJumpStrength(1);
		horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
		horse.setMaxHealth(40);
		horse.setHealth(40);
		
		UtilEnt.Vegetate(horse);
		
		_horseMap.put(player, horse);
		
		horse.getWorld().playSound(horse.getLocation(), Sound.ENTITY_HORSE_ANGRY, 2f, 1f);
	}

	@EventHandler
	public void horseUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		//Respawn
		Iterator<Player> respawnIterator = _deathTime.keySet().iterator();
		while (respawnIterator.hasNext())
		{
			Player player = respawnIterator.next();
			
			if (UtilTime.elapsed(_deathTime.get(player), 15000))
			{
				respawnIterator.remove();
				spawnHorse(player, true);
			}
		}
			
		//Update
		Iterator<Player> playerIterator = _horseMap.keySet().iterator();
		while (playerIterator.hasNext())
		{
			Player player = playerIterator.next();
			Horse horse = _horseMap.get(player);

			//Dead
			if (!horse.isValid() || horse.isDead())
			{
				horse.getWorld().playSound(horse.getLocation(), Sound.ENTITY_HORSE_DEATH, 1f, 1f);
				_deathTime.put(player, System.currentTimeMillis());
				playerIterator.remove();
				continue;
			}	
			
			//Return to Owner
			if (UtilMath.offset(horse, player) > 3)
			{
				if (UtilMath.offset(horse, player) > 24)
				{
					horse.teleport(player);
					continue;
				}
				
				float speed = Math.min(1f, (float)(UtilMath.offset(horse, player) - 5) / 8f);
				
				UtilEnt.CreatureMove(horse, player.getLocation().add(UtilAlg.getTrajectory(player, horse).multiply(2.5)), 1f + speed);
			}
			
			//Age
			if (horse.getTicksLived() > 900 && !horse.isAdult())
			{
				horse.setAdult();
				horse.getWorld().playSound(horse.getLocation(), Sound.ENTITY_HORSE_ANGRY, 2f, 1f);
				
				UtilPlayer.message(player, F.main("Game", "Your horse is now an adult!"));
				player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 2f);
				
				horse.getInventory().setArmor(new ItemStack(Material.IRON_HORSE_ARMOR));
			}
		}
	}


	@EventHandler
	public void heal(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
			return;

		for (Horse horse : _horseMap.values())
		{
			if (horse.getHealth() > 0)
				horse.setHealth(Math.min(horse.getMaxHealth(), horse.getHealth()+1));
		}
	}

	@EventHandler
	public void death(PlayerDeathEvent event)
	{
		Horse horse = _horseMap.remove(event.getEntity());

		if (horse == null)
			return;

		horse.remove();
	}

	@EventHandler
	public void damageRider(EntityDamageByEntityEvent event)
	{
		if (!(event.getEntity() instanceof Horse))
			return;
		
		Horse horse = (Horse)event.getEntity();
		
		if (!_horseMap.values().contains(horse))
			return;
		
		if (!horse.isAdult())
			event.setCancelled(true);
		
		Entity ent = event.getEntity().getPassenger();

		if (!(ent instanceof Player))
			return;

		if (!(event.getDamager() instanceof LivingEntity)) return;
		LivingEntity damager = (LivingEntity)event.getDamager();

		//Damage Event
		Manager.GetDamage().NewDamageEvent((Player)ent, damager, null,
				event.getCause(), event.getDamage() * 0.5, true, false, false,
				UtilEnt.getName(damager), event.getCause().name());
	}
	
	@EventHandler
	public void mountCancel(PlayerInteractEntityEvent event)
	{
		if (!(event.getRightClicked() instanceof Horse))
			return;
				
		if (!_horseMap.containsValue(event.getRightClicked()))
			return;

		Player player = event.getPlayer();
		Horse horse = (Horse)event.getRightClicked();
		
		if (horse.getOwner() != null && !horse.getOwner().equals(player))
		{
			UtilPlayer.message(player, F.main("Mount", "This is not your Horse!"));
			event.setCancelled(true);
		}
	}
}
