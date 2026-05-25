package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.SmashPerk;

public class PerkInferno extends SmashPerk
{
	private HashMap<Player, Long> _active = new HashMap<Player, Long>();
	
	public PerkInferno() 
	{
		super("Inferno", new String[] 
				{ 
				C.cYellow + "Hold Block" + C.cGray + " to use " + C.cGreen + "Inferno"
				});
	}
	
	@EventHandler
	public void EnergyUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!Kit.HasKit(player))
				continue;

			player.setExp((float) Math.min(0.999, player.getExp()+0.015));
		}
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
		
		_active.put(player, System.currentTimeMillis());

		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill("Inferno") + "."));
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
			
			if (!cur.isBlocking())
			{
				_active.remove(cur);
				continue;
			}
			
			cur.setExp(cur.getExp()-0.035f);

			if (cur.getExp() <= 0)
			{
				_active.remove(cur);
				continue;
			}

			//Fire
			Item fire = cur.getWorld().dropItem(cur.getEyeLocation(), ItemStackFactory.Instance.CreateStack(Material.BLAZE_POWDER));
			Manager.GetFire().Add(fire, cur, 0.7, 0, 0.5, 1, "Inferno");

			fire.teleport(cur.getEyeLocation());
			double x = 0.07 - (UtilMath.r(14)/100d);
			double y = 0.07 - (UtilMath.r(14)/100d);
			double z = 0.07 - (UtilMath.r(14)/100d);
			fire.setVelocity(cur.getLocation().getDirection().add(new Vector(x,y,z)).multiply(1.6));

			//Effect
			cur.getWorld().playSound(cur.getLocation(), Sound.ENTITY_GHAST_SHOOT, 0.1f, 1f);
		}
	}

}
