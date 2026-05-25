package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.HashMap;

import org.bukkit.Sound;
import org.bukkit.entity.Egg;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.SmashPerk;

public class PerkEggGun extends SmashPerk
{
	private HashMap<Player, Long> _active = new HashMap<Player, Long>();
	
	public PerkEggGun() 
	{
		super("Egg Blaster", new String[] 
				{ 
				C.cYellow + "Hold Block" + C.cGray + " to use " + C.cGreen + "Egg Blaster"
				});
	}
	
	
	@EventHandler
	public void Activate(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		
		if (UtilBlock.usable(event.getClickedBlock()))
			return;
		
		if (!event.getPlayer().getItemInHand().getType().toString().contains("_SWORD"))
			return;
		
		Player player = event.getPlayer();
		
		if (isSuperActive(event.getPlayer()))
			return;
		
		if (!Kit.HasKit(player))
			return;
		
		if (!Recharge.Instance.use(player, GetName(), 2500, true, true))
			return;
		
		_active.put(player, System.currentTimeMillis());

		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));
	}
	
	@EventHandler
	public void Update(UpdateEvent event)  
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player cur : UtilServer.getPlayers())
		{
			if (!isSuperActive(cur))
			{
				if (!_active.containsKey(cur))
					continue;
				
				if (!cur.isBlocking())
				{
					_active.remove(cur);
					continue;
				}
				
				if (UtilTime.elapsed(_active.get(cur), 750))
				{
					_active.remove(cur);
					continue;
				}
			}
			
			Vector offset = cur.getLocation().getDirection();
			if (offset.getY() < 0)
				offset.setY(0);
			
			Egg egg = cur.getWorld().spawn(cur.getLocation().add(0, 0.5, 0).add(offset), Egg.class);
			egg.setVelocity(cur.getLocation().getDirection().add(new Vector(0,0.2,0)));
			egg.setShooter(cur);
			 
			//Effect
			cur.getWorld().playSound(cur.getLocation(), Sound.ENTITY_CHICKEN_EGG, 0.5f, 1f);
		}
	}
	
	@EventHandler
	public void EggHit(EntityDamageByEntityEvent event)
	{
		if (event.getDamager() == null)
			return;
		
		if (!(event.getDamager() instanceof Egg))
			return;
		
		if (event.getDamage() >= 1)
			return;
		
		event.setCancelled(true);
		
		Egg egg = (Egg)event.getDamager();
		
		if (!(event.getEntity() instanceof LivingEntity)) return;
		LivingEntity target = (LivingEntity)event.getEntity();
		
		//Damage Event
		Manager.GetDamage().NewDamageEvent(target, (LivingEntity)egg.getShooter(), egg,
				DamageCause.PROJECTILE, 1, true, true, false,
				UtilEnt.getName((LivingEntity)egg.getShooter()), GetName());
		
		target.setVelocity(new Vector(0,0,0));
	}
}
