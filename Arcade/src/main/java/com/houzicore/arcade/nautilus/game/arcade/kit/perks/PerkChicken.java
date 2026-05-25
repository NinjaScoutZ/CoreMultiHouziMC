package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.Iterator;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;

import org.bukkit.inventory.ItemStack;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Egg;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;

public class PerkChicken extends Perk
{

	private NautHashMap<String, Creature> _activeKitHolders = new NautHashMap<String, Creature>();
	private NautHashMap<String, Integer> _failedAttempts = new NautHashMap<String, Integer>();
	
	private long _lastEgg = 0;

	public PerkChicken(ArcadeManager manager)
	{
		super("Eggman", new String[]
		{
				"Get a chicken that warns you of nearby enemies",
				"Your eggs give enemies Nausea for 3 seconds"
		});

	}

	public void spawnChicken(Player player, Location location)
	{

		if (_activeKitHolders.containsKey(player.getName()))
		{
			return;
		}

		Manager.GetGame().CreatureAllowOverride = true;

		Location loc = player.getLocation();
		Chicken c = loc.getWorld().spawn(loc.add(0, 1, 0), Chicken.class);
		c.setRemoveWhenFarAway(false);
		c.setMaxHealth(35.0D);
		c.setHealth(35.0D);

		c.setAdult();
		
		c.setCustomName(C.cAqua + UtilEnt.getName(player) + "'s Chicken");
		
		c.setCustomNameVisible(true);

		_activeKitHolders.put(player.getName(), c);
		_failedAttempts.put(player.getName(), 0);

		Manager.GetGame().CreatureAllowOverride = false;

	}

	@EventHandler
	public void chickenUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		int xDiff;
		int yDiff;
		int zDiff;

		Iterator<String> ownerIterator = _activeKitHolders.keySet().iterator();

		while (ownerIterator.hasNext())
		{
			String playerName = ownerIterator.next();
			Player owner = Bukkit.getPlayer(playerName);
			
			//Clean
			if (owner == null || !Manager.GetGame().IsAlive(owner))
			{
				Creature chicken = _activeKitHolders.get(playerName);
				chicken.remove();
				
				ownerIterator.remove();
				continue;
			}

			Creature chicken = _activeKitHolders.get(playerName);
			Location chickenSpot = chicken.getLocation();
			Location ownerSpot = owner.getLocation();
			xDiff = Math.abs(chickenSpot.getBlockX() - ownerSpot.getBlockX());
			yDiff = Math.abs(chickenSpot.getBlockY() - ownerSpot.getBlockY());
			zDiff = Math.abs(chickenSpot.getBlockZ() - ownerSpot.getBlockZ());

			if ((xDiff + yDiff + zDiff) > 4)
			{
				int xIndex = -1;
				int zIndex = -1;
				Block targetBlock = ownerSpot.getBlock().getRelative(xIndex,
						-1, zIndex);
				while (targetBlock.isEmpty() || targetBlock.isLiquid())
				{
					if (xIndex < 2)
						xIndex++;
					else if (zIndex < 2)
					{
						xIndex = -1;
						zIndex++;
					}
					else return;

					targetBlock = ownerSpot.getBlock().getRelative(xIndex, -1,
							zIndex);
				}

				double speed = 0.9d;

				if (_failedAttempts.get(playerName) > 4)
				{
					chicken.teleport(owner.getLocation().add(0, 2, 0));
					_failedAttempts.put(playerName, 0);
				}
				else if (!chicken.getPathfinder().moveTo(targetBlock.getLocation().add(0, 1, 0), speed))
				{
					if (chicken.getFallDistance() == 0)
					{
						_failedAttempts.put(playerName, _failedAttempts.get(playerName) + 1);
					}
				}

				else
				{
					_failedAttempts.put(playerName, 0);
				}
			}
			
			// Sentinel: Check for nearby enemies
			for (Player enemy : Manager.GetGame().GetPlayers(true))
			{
				if (enemy.equals(owner) || !Manager.GetGame().IsAlive(enemy))
					continue;
					
				if (com.houzicore.shared.common.util.UtilMath.offset(chicken.getLocation(), enemy.getLocation()) < 8)
				{
					if (com.houzicore.shared.common.util.UtilMath.r(20) == 0)
					{
						chicken.getWorld().playSound(chicken.getLocation(), Sound.ENTITY_CHICKEN_HURT, 1.0f, 1.0f);
					}
					break;
				}
			}
		}
	}
	
	@EventHandler
	public void dropEggs(UpdateEvent e)
	{
		if (e.getType() != UpdateType.FAST)
		{
			return;
		}
		
		if (!UtilTime.elapsed(_lastEgg, 8000))
			return;
		
		_lastEgg = System.currentTimeMillis();

		Iterator<String> ownerIterator = _activeKitHolders.keySet().iterator();

		while (ownerIterator.hasNext())
		{
			String playerName = ownerIterator.next();
			Player owner = Bukkit.getPlayer(playerName);

			Creature chicken = _activeKitHolders.get(playerName);
			Location chickenSpot = chicken.getLocation();

			Bukkit.getWorld(owner.getWorld().getName())
					.dropItemNaturally(chickenSpot, new ItemStack(Material.EGG))
					.setPickupDelay(30);
		}

	}

	@EventHandler
	public void onChickenDeath(EntityDeathEvent e)
	{

		if (!(e.getEntity() instanceof Chicken))
		{
			return;
		}

		Creature chicken = (Creature) e.getEntity();
		
		Iterator<String> ownerIterator = _activeKitHolders.keySet().iterator();

		while (ownerIterator.hasNext())
		{
			String playerName = ownerIterator.next();

			if (_activeKitHolders.get(playerName).equals(chicken))
			{
				ownerIterator.remove();
			}
		}
	}
	
	@EventHandler
	public void onEggHit(EntityDamageByEntityEvent event)
	{
		if (!(event.getDamager() instanceof Egg))
			return;
			
		Egg egg = (Egg) event.getDamager();
		if (!(egg.getShooter() instanceof Player))
			return;
			
		Player shooter = (Player) egg.getShooter();
		if (!Kit.HasKit(shooter))
			return;
			
		if (!(event.getEntity() instanceof Player))
			return;
			
		Player hit = (Player) event.getEntity();
		if (!Manager.GetGame().IsAlive(hit) || hit.equals(shooter))
			return;
			
		// Apply Nausea 3 seconds (60 ticks)
		hit.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 60, 0));
	}
}
