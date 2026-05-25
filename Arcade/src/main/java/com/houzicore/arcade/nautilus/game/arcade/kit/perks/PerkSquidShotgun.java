package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Sound;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.block.Block;
import org.bukkit.entity.Firework;
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
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.projectile.IThrown;
import com.houzicore.shared.core.projectile.ProjectileUser;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.recharge.RechargedEvent;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkSquidShotgun extends Perk implements IThrown
{
	private HashMap<Firework, Vector> _fireworks = new HashMap<Firework, Vector>();

	public PerkSquidShotgun() 
	{
		super("Ink Shotgun", new String[] 
				{
				C.cYellow + "Right-Click" + C.cGray + " with Axe to use " + C.cGreen + "Ink Shotgun"
				});
	}
	
	@EventHandler
	public void Recharge(RechargedEvent event)
	{
		if (!event.GetAbility().equals(GetName()))
			return;
		
		event.GetPlayer().playSound(event.GetPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 3f, 1f);
	}

	@EventHandler
	public void Shoot(PlayerInteractEvent event)
	{
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (UtilBlock.usable(event.getClickedBlock()))
			return;

		if (event.getPlayer().getItemInHand() == null)
			return;

		if (!event.getPlayer().getItemInHand().getType().toString().contains("_AXE"))
			return;

		Player player = event.getPlayer();

		if (!Kit.HasKit(player))
			return;

		if (!Recharge.Instance.use(player, GetName(), 2500, true, true))
			return;

		event.setCancelled(true);

		UtilInv.Update(player);

		//Firework
		FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.GREEN).with(Type.BURST).trail(false).build();

		for (int i=0 ; i<6 ; i++)
		{

			Vector random = new Vector(Math.random() - 0.5, Math.random() - 0.5, Math.random() - 0.5);
			random.normalize();
			random.multiply(0.3);
			
			try 
			{
				Vector vel = player.getLocation().getDirection().multiply(1.4).add(random);
				//Firework fw = Manager.GetFirework().launchFirework(player.getEyeLocation().subtract(0, 0.5, 0).add(player.getLocation().getDirection()), effect, vel);
				//_fireworks.put(fw, vel);
				
				//Projectile
// //Manager.getDamager().AddThrow(fw, player, this, -1, true, true, true, 3d, Manager.GetDisguise());
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
		
		
		//Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));

		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 0.75f);
	}
	

	
	@EventHandler
	public void FireworkUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		Iterator<Firework> fwIterator = _fireworks.keySet().iterator();
		
		while (fwIterator.hasNext())
		{
			Firework fw = fwIterator.next();
			
			if (!fw.isValid())
			{
				fwIterator.remove();
				continue;
			}
			
			fw.setVelocity(_fireworks.get(fw));
		}
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data) 
	{
		Explode(data);
		
		if (target == null)
			return;

		//Damage Event
		Manager.GetDamage().NewDamageEvent(target, data.GetThrower(), null,
				DamageCause.PROJECTILE, 12, false, true, false,
				UtilEnt.getName(data.GetThrower()), GetName());
		
		//Recharge
		if (data.GetThrower() instanceof Player)
		{
			Player player = (Player)data.GetThrower();
			Recharge.Instance.recharge(player, GetName());
			player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 3f, 1f);
		}
	}

	@Override
	public void Idle(ProjectileUser data) 
	{
		Explode(data);
	}

	@Override
	public void Expire(ProjectileUser data) 
	{
		Explode(data);
	}

	public void Explode(ProjectileUser data)
	{
		if (!(data.GetThrown() instanceof Firework))
		{
			data.GetThrown().remove();
			return;
		}
		
		Firework fw = (Firework)data.GetThrown();
		
		try 
		{
			//Manager.GetFirework().detonateFirework(fw);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
}
