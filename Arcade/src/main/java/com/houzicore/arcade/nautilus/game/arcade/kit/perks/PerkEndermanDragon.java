package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.HashMap;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.FireworkEffect.Type;
// NMS imports replaced with Bukkit types
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilFirework;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.kit.SmashPerk;

public class PerkEndermanDragon extends SmashPerk
{
	private HashMap<Player, EnderDragon> _dragons = new HashMap<Player, EnderDragon>();

	public PerkEndermanDragon() 
	{
		super("Ender Dragon", new String[] 
				{ 
				}, false);
	}
	
	@Override
	public void addSuperCustom(Player player)
	{
		Manager.GetGame().CreatureAllowOverride = true;
		EnderDragon dragon = player.getWorld().spawn(player.getLocation().add(0, 5, 0), EnderDragon.class);
		UtilEnt.Vegetate(dragon);
		Manager.GetGame().CreatureAllowOverride = false;
		
		dragon.setCustomName(C.cYellow + player.getName() + "'s Dragon");
		
		UtilFirework.playFirework(dragon.getLocation(), Type.BALL_LARGE, Color.BLACK, true, true);
		
		_dragons.put(player, dragon);
	}
	
	@Override
	public void removeSuperCustom(Player player)
	{
		EnderDragon dragon = _dragons.remove(player);
		if (dragon == null)
			return;
		
		player.leaveVehicle();
		dragon.remove();
	}
	
	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		for (Player player : _dragons.keySet())
		{
			EnderDragon dragon = _dragons.get(player);
			
			//Mount
			if (dragon.getPassenger() == null || !dragon.getPassenger().equals(player))
			{
				player.leaveVehicle();
				dragon.setPassenger(player);
			}
						
			//Move
			Location target = player.getLocation().add(player.getLocation().getDirection().multiply(40));
			dragon.setRotation(player.getLocation().getYaw(), player.getLocation().getPitch());
			dragon.setVelocity(player.getLocation().getDirection().multiply(1.5));
			dragon.setPhase(org.bukkit.entity.EnderDragon.Phase.CHARGE_PLAYER);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void explosionBlocks(EntityExplodeEvent event)
	{
		event.blockList().clear();
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void suffocationCancel(EntityDamageByEntityEvent event)
	{
		if (event.getCause() != DamageCause.SUFFOCATION)
			return;
		
		if (((Player) event.getEntity()) == null)
			return;
		
		if (isSuperActive(((Player) event.getEntity())))
			event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void dragonDamageeCancel(EntityDamageByEntityEvent event)
	{
		if (event.getDamager() == null)
			return;
		
		if (_dragons.values().contains(event.getDamager()))
			event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void dragonDamagerCancel(EntityDamageByEntityEvent event)
	{
		if (event.getDamager() == null)
			return;
		
		if (!_dragons.values().contains(event.getDamager()))
			return;
		
		event.setCancelled(true);
	}
	
	@EventHandler
	public void updateDamageAoe(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		for (Player player : _dragons.keySet())
		{
			EnderDragon dragon = _dragons.get(player);
			
			UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, dragon.getLocation(), 0, 0, 0, 0, 1,
					ViewDist.LONGER, UtilServer.getPlayers());
			
			for (Player other : Manager.GetGame().GetPlayers(true))
			{
				if (other.equals(player))
					continue;
				
				if (UtilMath.offset(dragon.getLocation().add(0, 4, 0), other.getLocation()) < 6 && Recharge.Instance.use(other, "Hit By Dragon", 1000, false, false))
				{
					//Damage Event
					Manager.GetDamage().NewDamageEvent(other, player, null,
							DamageCause.CUSTOM, 20, true, true, false,
							player.getName(), GetName());
				}
			}
		}
	}
	
	@EventHandler
	public void knockback(EntityDamageByEntityEvent event)
	{
		if (event.getCause().name() == null || !event.getCause().name().contains(GetName()))
			return;
		
  // /* event.AddKnockback(...) */, 4);
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void voidCancel(EntityDamageByEntityEvent event)
	{
		if (event.getCause() != DamageCause.VOID)
			return;
		
		if (!(event.getEntity() instanceof Player)) return;
		Player player = ((Player) event.getEntity());
		if (player == null)
			return;
		
		if (!isSuperActive(player))
			return;
		
		event.setCancelled(true);	
	}
}
