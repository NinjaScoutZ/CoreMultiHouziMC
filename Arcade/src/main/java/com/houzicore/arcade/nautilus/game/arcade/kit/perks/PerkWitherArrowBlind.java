package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;

import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.updater.UpdateType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkWitherArrowBlind extends Perk
{
	private ArrayList<Arrow> _arrows = new ArrayList<Arrow>();

	private int _proximityHit;
	
	public PerkWitherArrowBlind(int proximityHit) 
	{
		super("Smoke Arrow", new String[] 
				{
				"Your arrows give Blindness for 4 seconds"
				});
		
		_proximityHit = proximityHit;
	}

	@EventHandler
	public void FireBow(EntityShootBowEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;

		if (!(event.getProjectile() instanceof Arrow))
			return;

		if (!(event.getEntity() instanceof Player)) return;
		Player player = (Player)event.getEntity();
		
		if (!Kit.HasKit(player))
			return;

		//Start 
		_arrows.add((Arrow)event.getProjectile());
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void Damage(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (event.getDamager() == null)
			return;

		if (((Player) event.getDamager()) == null)
			return;

		if (!(event.getDamager() instanceof Arrow))
			return;
		
		Arrow arrow = (Arrow)event.getDamager();
		
		if (!_arrows.remove(arrow))
			return;
		
		Manager.GetCondition().Factory().Blind(GetName(), (LivingEntity)event.getEntity(), null, 4, 0, false, false, false);
		
		UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, arrow.getLocation(), 0, 0, 0, 0, 1,
				ViewDist.MAX, UtilServer.getPlayers());
		
		event.setCancelled(true);
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Iterator<Arrow> arrowIterator = _arrows.iterator(); arrowIterator.hasNext();) 
		{
			Arrow arrow = arrowIterator.next();
			
			//Proxy
			if (_proximityHit > 0 && getWitherTeam() != null)
			{
				boolean hit = false;
				for (Player player : getWitherTeam().GetPlayers(true))
				{
					if (UtilMath.offset(player.getLocation().add(0, 3, 0), arrow.getLocation()) < _proximityHit)
					{
						Manager.GetCondition().Factory().Blind(GetName(), player, null, 4, 0, false, false, false);
						hit = true;
					}
				}
				
				if (hit)
				{
					UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, arrow.getLocation(), 0, 0, 0, 0, 1,
							ViewDist.MAX, UtilServer.getPlayers());
					arrowIterator.remove();
					arrow.remove();
					continue;
				}
					
			}

			//Dead
			if (arrow.isDead() || !arrow.isValid() || arrow.getTicksLived() > 120 || arrow.isOnGround())
			{
				arrow.remove();
				arrowIterator.remove();
			}
			//Particle
			else
			{ { }
				UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, arrow.getLocation(), 0, 0, 0, 0, 1,
						ViewDist.MAX, UtilServer.getPlayers());
			}
		}
	}
	
	public GameTeam getWitherTeam()
	{
		if (Manager.GetGame() == null)
			return null;
		
		return Manager.GetGame().GetTeam(ChatColor.RED);
	}
}
