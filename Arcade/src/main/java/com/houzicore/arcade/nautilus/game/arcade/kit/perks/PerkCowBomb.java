package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Cow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.projectile.IThrown;
import com.houzicore.shared.core.projectile.ProjectileUser;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkCowBomb extends Perk implements IThrown
{
	public PerkCowBomb() 
	{
		super("Cow Bomb", new String[] 
				{
				C.cYellow + "Right-Click" + C.cGray + " with Axe to use " + C.cGreen + "Cow Bomb"
				});
	}


	@EventHandler
	public void Shoot(PlayerInteractEvent event)
	{
		if (event.isCancelled())
			return;

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

		if (!Recharge.Instance.use(player, GetName(), 3000, true, true))
			return;

		event.setCancelled(true);

		UtilInv.Update(player);

		Manager.GetGame().CreatureAllowOverride = true;
		Cow ent = player.getWorld().spawn(player.getEyeLocation().add(player.getLocation().getDirection()), Cow.class);
		Manager.GetGame().CreatureAllowOverride = false;
		ent.setBaby();
		ent.setAgeLock(true);
		ent.setMaxHealth(100);
		ent.setHealth(100);
		
		UtilAction.velocity(ent, player.getLocation().getDirection(), 1.4, false, 0, 0.3, 10, false);	

// Manager.getDamager().AddThrow(ent, player, this, -1, true, true, true,
// null, 1f, 1f,
// null, 1, UpdateType.SLOW,
// 0.5f);

		//Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));

		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_COW_AMBIENT, 2f, 1.5f);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data) 
	{
		Explode(data);

		if (target == null)
			return;

		//Damage Event
		Manager.GetDamage().NewDamageEvent(target, data.GetThrower(), null,
				DamageCause.PROJECTILE, 4, true, true, false,
				UtilEnt.getName(data.GetThrower()), GetName());
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
		//Effect
		data.GetThrown().getWorld().playSound(data.GetThrown().getLocation(), Sound.ENTITY_COW_HURT, 2f, 1.2f);
		data.GetThrown().getWorld().playSound(data.GetThrown().getLocation(), Sound.ENTITY_COW_HURT, 2f, 1.2f);
		
		data.GetThrown().getWorld().createExplosion(data.GetThrown().getLocation(), 0.5f);
		data.GetThrown().remove();
	}

	@EventHandler
	public void Knockback(EntityDamageByEntityEvent event)
	{
		if (event.getCause().name() == null || !event.getCause().name().contains(GetName()))
			return;

  // /* event.AddKnockback(...) */, 5);
	}
}
