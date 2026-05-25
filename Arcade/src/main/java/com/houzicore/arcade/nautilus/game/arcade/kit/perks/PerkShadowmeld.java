package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.HashSet;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkShadowmeld extends Perk
{
	private HashSet<Player> _active = new HashSet<Player>();
	
	public PerkShadowmeld() 
	{
		super("Shadowmeld", new String[] 
				{ 
				"Hold Crouch to become invisible.",
				"",
				"Shadowmeld ends if you attack or an",
				"enemy comes within 4 blocks of you."
				});
	}
		
	@EventHandler(priority = EventPriority.HIGH)
	public void ChargeBlock(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (Manager.GetGame() == null)
			return;

		for (Player cur : Manager.GetGame().GetPlayers(true))
		{	
			if (!Kit.HasKit(cur))
				continue;
			
			//Sneak
			if (!_active.contains(cur) && cur.isSneaking())
			{
				cur.setExp(Math.min(0.999f, cur.getExp() + (1f/60f)));
				
				if (cur.getExp() >= 0.99f)
				{
					Manager.GetCondition().Factory().Cloak(GetName(), cur, cur, 2.9, false, false);
				}
					
			}
			//End
			else
			{ { }
				end(cur);
			}
		}
	}
	
	private void end(Player cur)
	{
		_active.remove(cur);
		cur.setExp(0f);
		Manager.GetCondition().EndCondition(cur, null, GetName());
	}

	@EventHandler
	public void endProximity(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (Manager.GetGame() == null)
			return;
		
		for (Player cur : Manager.GetGame().GetPlayers(true))
		{
			if (!_active.contains(cur)) 	
				continue;
			
			//Proximity Decloak
			for (Player other : Manager.GetGame().GetPlayers(true))
			{
				if (other.equals(cur))
					continue;

				if (UtilMath.offset(cur, other) > 4)
					continue;

				end(cur);
				break;
			}

			Manager.GetCondition().Factory().Cloak(GetName(), cur, cur, 2.9, false, true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void endDamage(EntityDamageByEntityEvent event)
	{
		if (event.isCancelled())
			return;

		if (!(event.getEntity() instanceof Player)) return;
		Player damagee = ((Player) event.getEntity());
		if (damagee == null)	return;

		end(damagee);
	}

	@EventHandler
	public void EndInteract(PlayerInteractEvent event)
	{
		end(event.getPlayer());
	}
	
	@EventHandler
	public void EndBow(EntityShootBowEvent event)
	{
		end((Player)event.getEntity());
	}
}
