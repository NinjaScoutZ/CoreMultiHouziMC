package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import com.houzicore.shared.common.util.*;
import com.houzicore.shared.updater.*;
import com.houzicore.shared.updater.event.*;
//import com.houzicore.shared.combat.event.*;
//import com.houzicore.shared.damage.*;
import com.houzicore.arcade.nautilus.game.arcade.kit.*;

import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;


import java.util.*;

public class PerkSkeletons extends Perk
{
	public static class MinionSpawnEvent extends PlayerEvent
	{
		private static final HandlerList handlers = new HandlerList();

		public static HandlerList getHandlerList()
		{
			return handlers;
		}

		private final PerkSkeletons _perkSkeletons;

		public MinionSpawnEvent(Player who, PerkSkeletons perkSkeletons)
		{
			super(who);

			_perkSkeletons = perkSkeletons;
		}

		@Override
		public HandlerList getHandlers()
		{
			return getHandlerList();
		}

		public PerkSkeletons getPerkSkeletons()
		{
			return _perkSkeletons;
		}
	}

	private HashMap<Player, ArrayList<Skeleton>> _minions = new HashMap<Player, ArrayList<Skeleton>>();

	private boolean _name;
	private int _maxDist = 8;

	public PerkSkeletons(boolean name)
	{
		super("Skeleton Minons", new String[]
				{
						C.cGray + "Killing an opponent summons a skeletal minion."
				});

		_name = name;
	}

	@EventHandler
	public void MinionSpawn(PlayerDeathEvent event)
	{
		if (event.getEntity().getKiller() == null)
			return;

		if (!(event.getEntity() instanceof Player))
			return;

		Player killer = UtilPlayer.searchExact(event.getEntity().getKiller().getName());
		if (killer == null)
			return;

		if (!Kit.HasKit(killer))
			return;

		if (!(event.getEntity() instanceof Player)) return;
		Player killed = (Player) event.getEntity();

		Manager.GetGame().CreatureAllowOverride = true;
		Skeleton skel = killer.getWorld().spawn(killed.getLocation(), Skeleton.class);
		Manager.GetGame().CreatureAllowOverride = false;

		UtilEnt.removeGoalSelectors(skel);

		skel.setMaxHealth(30.0);
		skel.setHealth(30);

		ItemStack handItem = killed.getInventory().getItemInMainHand();
		skel.getEquipment().setItemInMainHand(handItem);
		skel.getEquipment().setHelmet(killed.getInventory().getHelmet());
		skel.getEquipment().setChestplate(killed.getInventory().getChestplate());
		skel.getEquipment().setLeggings(killed.getInventory().getLeggings());
		skel.getEquipment().setBoots(killed.getInventory().getBoots());


		event.getDrops().remove(handItem);
		event.getDrops().remove(killed.getInventory().getHelmet());
		event.getDrops().remove(killed.getInventory().getChestplate());
		event.getDrops().remove(killed.getInventory().getLeggings());
		event.getDrops().remove(killed.getInventory().getBoots());


		skel.getEquipment().setItemInMainHandDropChance(1f);
		skel.getEquipment().setHelmetDropChance(1f);
		skel.getEquipment().setChestplateDropChance(1f);
		skel.getEquipment().setLeggingsDropChance(1f);
		skel.getEquipment().setBootsDropChance(1f);


		if (_name)
		{
			skel.setCustomName("Skeletal " + UtilEnt.getName(event.getEntity()));
			skel.setCustomNameVisible(true);
		}

		if (!_minions.containsKey(killer))
			_minions.put(killer, new ArrayList<Skeleton>());

		_minions.get(killer).add(skel);

		killer.playSound(killer.getLocation(), Sound.ENTITY_SKELETON_HURT, 1f, 1f);


		Bukkit.getPluginManager().callEvent(new MinionSpawnEvent(killer, this));
	}

	@EventHandler
	public void TargetCancel(EntityTargetEvent event)
	{
		if (!_minions.containsKey(event.getTarget()))
			return;

		if (_minions.get(event.getTarget()).contains(event.getEntity()))
			event.setCancelled(true);

		for (Player player : _minions.keySet())
		{
			for (Skeleton skel : _minions.get(player))
			{
				if (event.getEntity().equals(skel))
				{
					if (UtilMath.offset(skel, player) > _maxDist)
					{

					}
				}
			}
		}
	}

	@EventHandler
	public void MinionUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		for (Player player : _minions.keySet())
		{
			Iterator<Skeleton> skelIterator = _minions.get(player).iterator();

			while (skelIterator.hasNext())
			{
				Skeleton skel = skelIterator.next();

				//Dead
				if (!skel.isValid())
				{
					skelIterator.remove();
					continue;
				}

				//Return to Owner
				double range = 4;
				if (skel.getTarget() != null)
				{
					range = _maxDist;
				}


				if (UtilMath.offset(skel, player) > range)
				{
					float speed = 1.25f;
					if (player.isSprinting())
						speed = 1.75f;

					//Move
					Location target = skel.getLocation().add(UtilAlg.getTrajectory(skel, player).multiply(3));

					skel.getPathfinder().moveTo(target, speed);

					skel.setTarget(null);

				}
			}
		}
	}

	@EventHandler
	public void Heal(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		for (ArrayList<Skeleton> skels : _minions.values())
		{
			for (Skeleton skel : skels)
			{
				if (skel.getHealth() > 0)
					skel.setHealth(Math.min(skel.getMaxHealth(), skel.getHealth() + 1));

			}
		}
	}

	public boolean IsMinion(Entity ent)
	{
		for (ArrayList<Skeleton> skels : _minions.values())
		{
			for (Skeleton skel : skels)
			{
				if (ent.equals(skel))
				{
					return true;
				}
			}
		}

		return false;
	}

	@EventHandler
	public void Combust(EntityCombustEvent event)
	{
		if (IsMinion(event.getEntity()))
			event.setCancelled(true);
	}

	@EventHandler
	public void Damage(EntityDamageByEntityEvent event)
	{
		if (event.getDamager() == null)
			return;

		if (!IsMinion(event.getDamager()))
			return;

		double damage = 4;

		if (event.getDamager() instanceof Skeleton)
		{
			Skeleton skel = (Skeleton) event.getDamager();

			if (skel.getEquipment().getItemInMainHand() != null)
			{
				if (skel.getEquipment().getItemInMainHand().getType() == Material.STONE_SWORD) damage = 5;
				else if (skel.getEquipment().getItemInMainHand().getType() == Material.IRON_SWORD) damage = 6;
				else if (skel.getEquipment().getItemInMainHand().getType() == Material.GOLDEN_SWORD) damage = 6;
				else if (skel.getEquipment().getItemInMainHand().getType() == Material.DIAMOND_SWORD) damage = 7;

				else if (skel.getEquipment().getItemInMainHand().getType() == Material.IRON_AXE) damage = 5;
				else if (skel.getEquipment().getItemInMainHand().getType() == Material.GOLDEN_AXE) damage = 5;
				else if (skel.getEquipment().getItemInMainHand().getType() == Material.DIAMOND_AXE) damage = 6;
			}

		}

		if (event.getDamager() != null)
			damage = 6;

  // /* event.AddMod(...) */, false);
  // /* event.AddMod(...) */;
	}

	@EventHandler
	public void PlayerDeath(PlayerDeathEvent event)
	{
		ArrayList<Skeleton> skels = _minions.remove(event.getEntity());

		if (skels == null)
			return;

		for (Skeleton skel : skels)
			skel.remove();

		skels.clear();
	}

	public List<Skeleton> getSkeletons(Player player)
	{
		return _minions.get(player);
	}
}
