package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import com.houzicore.shared.api.disguise.DisguiseArchetype;
import com.houzicore.shared.api.disguise.DisguiseRequest;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.kit.SmashKit;
import com.houzicore.arcade.nautilus.game.arcade.kit.SmashPerk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.data.SonicBoomData;

public class PerkBatForm extends SmashPerk
{
	private ArrayList<SonicBoomData> _sonic = new ArrayList<SonicBoomData>();
	
	public PerkBatForm() 
	{
		super("Bat Form", new String[] 
				{ 
				}, false);
	}

	@Override
	public void addSuperCustom(Player player)
	{
		Manager.GetDisguise().getService().clear(player);
		Manager.GetDisguise().getService().apply(player,
				new DisguiseRequest(
						player.getUniqueId(),
						DisguiseArchetype.MOB,
						"BAT",
						true,
						false,
						false));
	}

	@Override
	public void removeSuperCustom(Player player)
	{
		Manager.GetDisguise().getService().clear(player);
		Manager.GetDisguise().getService().apply(player,
				new DisguiseRequest(
						player.getUniqueId(),
						DisguiseArchetype.MOB,
						"WITCH",
						true,
						false,
						false));
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void attackCancel(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled())
			return;

		if (!(event.getDamager() instanceof Player)) return;
		Player player = ((Player) event.getDamager());
		if (player == null)
			return;

		if (!isSuperActive(player))
			return;

		if (event.getCause() != DamageCause.ENTITY_ATTACK)
			return;

		event.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.LOW)	//Happen before activation of Super
	public void sonicBoom(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (!isSuperActive(player))
			return;

		if (event.getAction() == Action.PHYSICAL)
			return;
		
		if (!Recharge.Instance.use(player, GetName() + " Screech", 1200, false, false))
			return;

		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_HURT, 1f, 0.75f);
		
		_sonic.add(new SonicBoomData(player));
	}
	
	@EventHandler
	public void sonicBoomUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		Iterator<SonicBoomData> sonicIter = _sonic.iterator();
		
		while (sonicIter.hasNext())
		{
			SonicBoomData data = sonicIter.next();
			
			//Time Boom
			if (UtilTime.elapsed(data.Time, 12000))
			{
				sonicIter.remove();
				explode(data);
				continue;
			}
			
			//Block Boom
			if (!UtilBlock.airFoliage(data.Location.getBlock()))
			{
				sonicIter.remove();
				explode(data);
				continue;
			}
			
			//Proxy Boom
			for (Player player : Manager.GetGame().GetPlayers(true))
			{
				if (Manager.isSpectator(player))
					continue;
				
				if (player.equals(data.Shooter))
					continue;
				
				if (UtilMath.offset(player.getLocation().add(0, 1, 0), data.Location) < 4)
				{
					sonicIter.remove();
					explode(data);
					continue;
				}
			}
			
			//Move
			data.Location.add(data.Direction.clone().multiply(0.75));
			
			//Effect
			UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, data.Location, 0, 0, 0, 0, 1,
					ViewDist.MAX, UtilServer.getPlayers());
			data.Location.getWorld().playSound(data.Location, Sound.BLOCK_FIRE_EXTINGUISH, 1f, 2f);
		}
	}

	private void explode(SonicBoomData data)
	{
		//Effect
		UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, data.Location, 0, 0, 0, 0, 1,
				ViewDist.MAX, UtilServer.getPlayers());
		data.Location.getWorld().playSound(data.Location, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1.5f);
		
		//Damage
		HashMap<LivingEntity, Double> targets = UtilEnt.getInRadius(data.Location, 10);
		for (LivingEntity cur : targets.keySet())
		{
			Manager.GetDamage().NewDamageEvent(cur, data.Shooter, null,
					DamageCause.CUSTOM, 12 * targets.get(cur) + 0.5, true, false, false,
					data.Shooter.getName(), GetName());
		}
	}

	@EventHandler
	public void flap(PlayerToggleFlightEvent event)
	{
		Player player = event.getPlayer();

		if (Manager.isSpectator(player))
			return;

		if (!isSuperActive(player))
			return;
		
		if (player.getGameMode() == GameMode.CREATIVE)
			return;

		event.setCancelled(true);
		player.setFlying(false);

		//Disable Flight
		player.setAllowFlight(false);

		//Velocity
		UtilAction.velocity(player, player.getLocation().getDirection(), 0.8, false, 0, 0.8, 1, true);

		//Sound
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, (float)(0.3 + player.getExp()), (float)(Math.random()/2+0.5));

		//Set Recharge
		Recharge.Instance.use(player, GetName() + " Flap", 40, false, false);
	}
	
	@EventHandler
	public void flapRecharge(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : ((SmashKit)Kit).getSuperActive())
		{
			if (Manager.isSpectator(player))
				continue;

			if (UtilEnt.isGrounded(player) || UtilBlock.solid(player.getLocation().getBlock().getRelative(BlockFace.DOWN))) 
			{
				player.setAllowFlight(true);
			}
			else if (Recharge.Instance.usable(player, GetName() + " Flap"))
			{
				player.setAllowFlight(true);
			}
		}
	}
	
	@EventHandler
	public void knockback(EntityDamageByEntityEvent event)
	{
		if (event.getCause().name() == null || !event.getCause().name().contains(GetName()))
			return;
		
  // /* event.AddKnockback(...) */, 2);
	}
}
