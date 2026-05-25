package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.projectile.IThrown;
import com.houzicore.shared.core.projectile.ProjectileUser;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkWeb extends Perk implements IThrown
{
	private int _spawnRate;
	private int _max;
	
	public PerkWeb(int spawnRate, int max) 
	{
		super("Bomber", new String[] 
				{
				C.cGray + "Receive 1 Web every " + spawnRate + " seconds. Maximum of " + max + ".",
				C.cYellow + "Click" + C.cGray + " with Web to " + C.cGreen + "Throw Web"
				});
		
		_spawnRate = spawnRate;
		_max = max;
	}
	
	@EventHandler
	public void Spawn(UpdateEvent event)
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

			if (UtilInv.contains(cur, Material.COBWEB, (byte)0, _max))
				continue;

			//Add
			cur.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.COBWEB));
		}
	}

	@EventHandler
	public void Throw(PlayerInteractEvent event)
	{
		if (event.getPlayer().getItemInHand() == null)
			return;
		
		if (event.getPlayer().getItemInHand().getType() == Material.COBWEB)
		{
			if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK &&
				event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
				return;
		}
		else if (event.getPlayer().getItemInHand().getType().toString().contains("_AXE"))
		{
			if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
				return;	
		}
		else
		{
			return;
		}
		
		Player player = event.getPlayer();
		
		if (!Kit.HasKit(player))
			return;
		
		event.setCancelled(true);
		
		UtilInv.remove(player, Material.COBWEB, (byte)0, 1);
		UtilInv.Update(player);
		
		org.bukkit.entity.Item ent = player.getWorld().dropItem(player.getEyeLocation(), ItemStackFactory.Instance.CreateStack(Material.COBWEB));
		UtilAction.velocity(ent, player.getLocation().getDirection(), 0.8, false, 0, 0.2, 10, false);
// Manager.getDamager().AddThrow(ent, player, this, -1, true, true, true, false, 0.5f);
	}
	
	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data) 
	{
		if (target != null)
		{
			if (target instanceof Player)
			{
				if (!Manager.GetGame().IsAlive((Player)target))
				{
					return;
				}
			}
		}
		
		Web(data);
	}

	@Override
	public void Idle(ProjectileUser data) 
	{
		Web(data);
	}

	@Override
	public void Expire(ProjectileUser data) 
	{
		Web(data);
	}
	
	public void Web(ProjectileUser data)
	{
		Location loc = data.GetThrown().getLocation();
		data.GetThrown().remove();
		
		Manager.GetBlockRestore().Add(loc.getBlock(), 30, (byte)0, 4000);
	}
}
