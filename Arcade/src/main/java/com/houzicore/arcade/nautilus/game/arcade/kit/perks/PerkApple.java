package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import org.bukkit.Material;
import org.bukkit.Sound;

import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.projectile.IThrown;
import com.houzicore.shared.core.projectile.ProjectileUser;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;


public class PerkApple extends Perk implements IThrown
{

	public PerkApple(ArcadeManager manager) 
	{
		super("Apple Thrower",  new String[] 
				{
				C.cGray + "Receive 1 Apple every 10 seconds",
				C.cYellow + "Left-Click" + C.cGray + " with Apple to " + C.cGreen + "Throw Apple",
				});
	}
	
	@EventHandler
	public void AppleSpawn(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;
		
		if (Manager.GetGame() == null)
			return;
			
		for (Player player : Manager.GetGame().GetPlayers(true))
		{
			if (!Kit.HasKit(player))
				continue;
			
			if (!Manager.GetGame().IsAlive(player))
				continue;
			
			if (!Recharge.Instance.use(player, "Apple Spawn", 10000, false, false))
				continue;
			
			player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.APPLE));
			player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 2f, 1f);

		}
	}
	
	@EventHandler
	public void ThrowApple(PlayerInteractEvent event)
	{
		if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK)
			return;
		
		if (event.getPlayer().getInventory().getItemInMainHand() == null)
			return;
		
		if (event.getPlayer().getInventory().getItemInMainHand().getType() != Material.APPLE)
			return;

		
		Player player = event.getPlayer();
		
		if (!Kit.HasKit(player))
			return;
		
		event.setCancelled(true);
		
		UtilInv.remove(player, Material.APPLE, (byte)0, 1);
		UtilInv.Update(player);
		
		org.bukkit.entity.Item ent = player.getWorld().dropItem(player.getEyeLocation(), ItemStackFactory.Instance.CreateStack(Material.APPLE));
		UtilAction.velocity(ent, player.getLocation().getDirection(), 1.2, false, 0, 0.2, 10, false);
// Manager.getDamager().AddThrow(ent, player, this, -1, true, true, true, false, 0.5f);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data)
	{
		if (target == null)
			return;
		
		if (target instanceof Player)
		{
			if (!Manager.GetGame().IsAlive((Player)target))
			{
				return;
			}
		}

		//Damage Event
		Manager.GetDamage().NewDamageEvent(target, data.GetThrower(), null,
				DamageCause.CUSTOM, 3, true, false, false,
				UtilEnt.getName(data.GetThrower()), GetName());

		//Effect
		data.GetThrown().getWorld().playSound(data.GetThrown().getLocation(), Sound.ENTITY_CHICKEN_EGG, 1f, 1.6f);

		//Re-Drop
		if (data.GetThrown() instanceof org.bukkit.entity.Item)
			data.GetThrown().getWorld().dropItem(data.GetThrown().getLocation(), ItemStackFactory.Instance.CreateStack(Material.APPLE)).setPickupDelay(60);


		data.GetThrown().remove();
	}

	@Override
	public void Idle(ProjectileUser data) 
	{
		
	}

	@Override
	public void Expire(ProjectileUser data) 
	{
		
	}
}
