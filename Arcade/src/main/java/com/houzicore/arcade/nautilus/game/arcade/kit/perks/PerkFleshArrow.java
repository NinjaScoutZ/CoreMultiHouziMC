package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.kit.SmashPerk;

public class PerkFleshArrow extends SmashPerk
{
	private HashSet<Entity> _arrows = new HashSet<Entity>();

	public PerkFleshArrow() 
	{
		super("Flesh Arrow", new String[] 
				{
				C.cYellow + "Left-Click" + C.cGray + " with Bow to " + C.cGreen + "Flesh Arrow"
				});
	}

	@EventHandler
	public void fire(PlayerInteractEvent event)
	{
		if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK)
			return;

		if (event.getPlayer().getItemInHand() == null)
			return;

		if (event.getPlayer().getItemInHand().getType() != Material.BOW)
			return;

		Player player = event.getPlayer();
		
		if (!Kit.HasKit(player))
			return;

		if (!Recharge.Instance.use(player, GetName(), 8000, true, true))
			return;

		//Arrow
		Arrow arrow = player.launchProjectile(Arrow.class);
		arrow.setVelocity(player.getLocation().getDirection().multiply(3));
		_arrows.add(arrow);

		//Inform
		UtilPlayer.message(player, F.main("Game", "You fired " + F.skill(GetName()) + "."));	
	}

	@EventHandler
	public void hit(ProjectileHitEvent event)
	{
		if (!_arrows.remove(event.getEntity()))
			return;

		
		event.getEntity().remove();
	}
	
	@EventHandler
	public void damage(EntityDamageByEntityEvent event)
	{
		if (event.getDamager() == null)
			return;
		
		if (!_arrows.contains(event.getDamager()))
			return;
		
		if (!(event.getEntity() instanceof LivingEntity)) return;
		LivingEntity ent = (LivingEntity)event.getEntity();
		
		Arrow arrow = (Arrow)event.getDamager();
		if (!(arrow.getShooter() instanceof LivingEntity)) return;
		LivingEntity shooter = (LivingEntity)arrow.getShooter();
		
		Manager.GetCondition().Factory().Slow(GetName(), ent, shooter, 4, 3, false, false, false, false);
		
		ent.setVelocity(new Vector(0,-0.5,0));
	}

	@EventHandler
	public void clean(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		for (Iterator<Entity> arrowIterator = _arrows.iterator(); arrowIterator.hasNext();) 
		{
			Entity arrow = arrowIterator.next();

			if (!arrow.isValid())
				arrowIterator.remove();
		}
	}
}
