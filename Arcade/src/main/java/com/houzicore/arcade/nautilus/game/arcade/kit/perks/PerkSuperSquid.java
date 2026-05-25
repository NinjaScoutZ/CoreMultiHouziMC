package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.HashMap;

import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.kit.SmashPerk;

public class PerkSuperSquid extends SmashPerk
{
	private HashMap<Player, Long> _active = new HashMap<Player, Long>();
	
	public PerkSuperSquid() 
	{
		super("Super Squid", new String[] 
				{ 
				C.cYellow + "Hold Block" + C.cGray + " to use " + C.cGreen + "Super Squid",
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
		
		if (isSuperActive(player))
			return;
		
		if (!Kit.HasKit(player))
			return;
		
		if (!Recharge.Instance.use(player, GetName(), 8000, true, true))
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
			if (!_active.containsKey(cur))
				continue;
			
			if (isSuperActive(cur))
				return;
			
			if (!cur.isBlocking())
			{
				_active.remove(cur);
				continue;
			}
			
			if (UtilTime.elapsed(_active.get(cur), 1100))
			{
				_active.remove(cur);
				continue;
			}

			UtilAction.velocity(cur, 0.6, 0.1, 1, true);
			
			cur.getWorld().playSound(cur.getLocation(), Sound.ENTITY_GENERIC_SPLASH, 0.2f, 1f);
			cur.getWorld().playEffect(cur.getLocation(), Effect.STEP_SOUND, 8);
		}
	}

	@EventHandler
	public void DamageCancel(EntityDamageByEntityEvent event)
	{
		if (_active.containsKey(event.getEntity()))
			event.setCancelled(true);
	}
}
