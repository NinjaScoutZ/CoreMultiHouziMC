package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.DyeColor;
import org.bukkit.Particle;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;

import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.updater.UpdateType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;


public class PerkWolfPet extends Perk
{
	private HashMap<Player, ArrayList<Wolf>> _wolfMap = new HashMap<Player, ArrayList<Wolf>>();

	private HashMap<Wolf, Long> _tackle = new HashMap<Wolf, Long>();

	private int _spawnRate;
	private int _max;
	private boolean _baby;
	private boolean _name;

	public PerkWolfPet(int spawnRate, int max, boolean baby, boolean name) 
	{
		super("Wolf Master", new String[] 
				{
				C.cGray + "Spawn 1 Wolf every " + spawnRate + " seconds. Maximum of " + max + ".",
				C.cYellow + "Right-Click" + C.cGray + " with Sword/Axe to use " + C.cGreen + "Wolf Tackle",
				});

		_spawnRate = spawnRate;
		_max = max;
		_baby = baby;
		_name = name;
	}

	@Override
	public void Apply(Player player) 
	{
		Recharge.Instance.use(player, GetName(), _spawnRate*1000, false, false);
	}

	@EventHandler
	public void CubSpawn(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		for (Player cur : UtilServer.getPlayers())
		{
			if (!Kit.HasKit(cur))
				continue;

			if (!Manager.GetGame().IsAlive(cur))
				continue;

			if (!Recharge.Instance.use(cur, GetName(), _spawnRate*1000, false, false))
				continue;

			if (!_wolfMap.containsKey(cur))
				_wolfMap.put(cur, new ArrayList<Wolf>());

			if (_wolfMap.get(cur).size() >= _max)
				continue;

			Manager.GetGame().CreatureAllowOverride = true;
			Wolf wolf = cur.getWorld().spawn(cur.getLocation(), Wolf.class);
			Manager.GetGame().CreatureAllowOverride = false;

			wolf.setOwner(cur);
			wolf.setCollarColor(DyeColor.GREEN);
			wolf.getWorld().spawnParticle(Particle.HEART, wolf.getLocation().add(0, 1, 0), 5, 0.5, 0.5, 0.5);
			wolf.setAngry(true);
			wolf.setMaxHealth(14.0);
			wolf.setHealth(14.0);


			if (_baby)
				wolf.setBaby();

			if (_name)
			{
				wolf.setCustomName(cur.getName() + "'s Wolf");
			}


			_wolfMap.get(cur).add(wolf);

			cur.playSound(cur.getLocation(), Sound.ENTITY_WOLF_GROWL, 1f, 1f);

		}
	}

	@EventHandler
	public void CubTargetCancel(EntityTargetEvent event)
	{
		if (!_wolfMap.containsKey(event.getTarget()))
			return;

		if (_wolfMap.get(event.getTarget()).contains(event.getEntity()))
			event.setCancelled(true);
	}

	@EventHandler
	public void CubUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		for (Player player : _wolfMap.keySet())
		{
			Iterator<Wolf> wolfIterator = _wolfMap.get(player).iterator();

			while (wolfIterator.hasNext())
			{
				Wolf wolf = wolfIterator.next();

				//Dead
				if (!wolf.isValid())
				{
					wolf.getWorld().playSound(wolf.getLocation(), Sound.ENTITY_WOLF_DEATH, 1f, 1f);
					Recharge.Instance.useForce(player, GetName(), _spawnRate*1000);

					wolfIterator.remove();
					continue;
				}	
				
				if (player.isSneaking())
				{
					wolf.setTarget(null);
				}

									
				//Return to Owner
				double range = 0.5;
				if (wolf.getTarget() != null)
					range = 12;
				
				Location target = player.getLocation().add(player.getLocation().getDirection().multiply(3));
				target.setY(player.getLocation().getY());
				
				if (UtilMath.offset(wolf.getLocation(), target) > range)
				{
					float speed = 1f;
					if (player.isSprinting())
						speed = 1.4f;

					//Move
					wolf.getPathfinder().moveTo(target, speed);

					wolf.setTarget(null);

				}
			}
		}
	}

	@EventHandler
	public void CubStrikeTrigger(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;

		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (event.getPlayer().getItemInHand() == null)
			return;

		if (!event.getPlayer().getItemInHand().getType().toString().contains("_AXE") && !event.getPlayer().getItemInHand().getType().toString().contains("_SWORD"))
			return; 

		Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;
		
		if (!_wolfMap.containsKey(player) || _wolfMap.get(player).isEmpty())
		{
			UtilPlayer.message(player, F.main("Game", "You have no Wolf Cubs."));
			return;
		}

		if (!Recharge.Instance.use(player, "Cub Strike", 4000, true, true))
			return;

		Wolf wolf = _wolfMap.get(player).get(UtilMath.r(_wolfMap.get(player).size()));
		
		UtilAction.velocity(wolf, player.getLocation().getDirection(), 1.4, false, 0, 0.2, 1.2, true);
		
		wolf.getWorld().spawnParticle(Particle.SMOKE, wolf.getLocation().add(0, 1, 0), 7, 0.3, 0.3, 0.3, 0.02);
		
		player.getWorld().playSound(wolf.getLocation(), Sound.ENTITY_WOLF_AMBIENT, 1f, 1.2f);


		//Record
		_tackle.put(wolf, System.currentTimeMillis());

		//Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill("Cub Strike") + "."));
	}

	@EventHandler
	public void CubStrikeEnd(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		//Collide
		Iterator<Wolf> wolfIterator = _tackle.keySet().iterator();

		while (wolfIterator.hasNext())
		{
			Wolf wolf = wolfIterator.next();

			for (Player other : Manager.GetGame().GetPlayers(true))
				if (!Manager.isSpectator(other))
					if (UtilEnt.hitBox(wolf.getLocation(), other, 2, null))
					{
						if (other.equals(wolf.getOwner()))
							continue;
						
						CubStrikeHit((Player)wolf.getOwner(), other, wolf);
						wolfIterator.remove();
						return;
					}
			
			if (!UtilEnt.isGrounded(wolf))
				continue;

			if (!UtilTime.elapsed(_tackle.get(wolf), 1000))  
				continue;

			wolfIterator.remove();	
		}	
	}
	
	public void CubStrikeHit(Player damager, LivingEntity damagee, Wolf wolf)
	{
		//Damage Event
		wolf.setTarget(damagee);
		
		//Sound
		damagee.getWorld().playSound(damagee.getLocation(), Sound.ENTITY_WOLF_AMBIENT, 1.5f, 1.5f);


		//Slow
		Manager.GetCondition().Factory().Slow(GetName(), damagee, damager, 4, 1, false, false, true, false);

		//Inform
		UtilPlayer.message(damager, F.main("Game", "You hit " + F.name(UtilEnt.getName(damagee)) + " with " + F.skill("Wolf Tackle") + "."));
		UtilPlayer.message(damagee, F.main("Game", F.name(damager.getName()) + " hit you with " + F.skill("Wolf Tackle") + "."));
	}
	
	@EventHandler
	public void CubHeal(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		for (ArrayList<Wolf> wolves : _wolfMap.values())
		{
			for (Wolf wolf : wolves)
			{
				if (wolf.getHealth() > 0)
					wolf.setHealth(Math.min(wolf.getMaxHealth(), wolf.getHealth()+1));
			}
		}
	}
	
	@EventHandler
	public void PlayerDeath(PlayerDeathEvent event)
	{
		ArrayList<Wolf> wolves = _wolfMap.remove(event.getEntity());
		
		if (wolves == null)
			return;
		
		for (Wolf wolf : wolves)
			wolf.remove();
		
		wolves.clear();
	}

	@EventHandler
	public void PlayerQuit(PlayerQuitEvent event)
	{
		ArrayList<Wolf> wolves = _wolfMap.remove(event.getPlayer());
		
		if (wolves == null)
			return;
		
		for (Wolf wolf : wolves)
			wolf.remove();
		
		wolves.clear();
	}
	
	public boolean IsMinion(Entity ent)
	{
		for (ArrayList<Wolf> minions : _wolfMap.values())
		{
			for (Wolf minion : minions)
			{
				if (ent.equals(minion))
				{
					return true;
				}
			}
		}

		return false;
	}
	
	@EventHandler
	public void Damage(EntityDamageByEntityEvent event)
	{
		if (event.getDamager() == null)
			return;

		if (!IsMinion(event.getDamager()))
			return;

		double damage = 3;

  // /* event.AddMod(...) */, false);
  // /* event.AddMod(...) */;
	}
}
