package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.HashMap;

import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilGear;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkSeismicSlamSND extends Perk
{	
	private HashMap<LivingEntity, Long> _live = new HashMap<LivingEntity, Long>();

	public PerkSeismicSlamSND() 
	{
		super("Seismic Slam", new String[]  
				{
				C.cYellow + "Right-Click" + C.cGray + " with Sword to " + C.cGreen + "Seismic Slam"
				});
	}

	@EventHandler
	public void leap(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;

		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (!UtilGear.isSword(event.getPlayer().getItemInHand()))
			return;

		Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;

		if (!Recharge.Instance.use(player, GetName(), 20000, true, true))
			return;

		//Action
		Vector vec = player.getLocation().getDirection();
		if (vec.getY() < 0)
			vec.setY(vec.getY() * -1);

		UtilAction.velocity(player, vec, 1, true, 1, 0, 1, true);

		//Record
		_live.put(player, System.currentTimeMillis());

		//Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));
	}
	
	@EventHandler
	public void slam(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!UtilEnt.isGrounded(player))
				continue;

			if (!_live.containsKey(player))
				continue;

			if (!UtilTime.elapsed(_live.get(player), 1000))  
				continue;

			_live.remove(player);
			
			//Action
			int damage = 8;
			double range = 8;
			
			HashMap<LivingEntity, Double> targets = UtilEnt.getInRadius(player.getLocation(), range);
			for (LivingEntity cur : targets.keySet())
			{
				if (cur.equals(player))
					continue;

				//Damage Event
				Manager.GetDamage().NewDamageEvent(cur, player, null,
						DamageCause.CUSTOM, damage * targets.get(cur) + 0.5, true, true, false,
						player.getName(), GetName());

				//Condition
				Manager.GetCondition().Factory().Falling(GetName(), cur, player, 10, false, true);

				//Inform
				if (cur instanceof Player)
					UtilPlayer.message((Player)cur, F.main("Game", F.name(player.getName()) +" hit you with " + F.skill(GetName()) + "."));	
			}
			
			//Effect
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, 2f, 0.2f);
			for (Block cur : UtilBlock.getInRadius(player.getLocation(), 4d).keySet())
				if (UtilBlock.airFoliage(cur.getRelative(BlockFace.UP)) && !UtilBlock.airFoliage(cur))
					cur.getWorld().playEffect(cur.getLocation(), Effect.STEP_SOUND, com.houzicore.shared.common.util.IdUtil.getTypeId(cur));
		}	
	}
	
	@EventHandler
	public void knockback(EntityDamageByEntityEvent event)
	{
		if (event.getCause().name() == null || !event.getCause().name().contains(GetName()))
			return;
		
  // /* event.AddKnockback(...) */, 2.4);
	}
}
