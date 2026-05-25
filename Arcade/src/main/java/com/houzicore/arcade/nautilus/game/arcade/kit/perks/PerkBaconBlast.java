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
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.projectile.IThrown;
import com.houzicore.shared.core.projectile.ProjectileUser;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class PerkBaconBlast extends Perk implements IThrown
{
	public PerkBaconBlast() 
	{
		super("Bacon Blast", new String[] 
				{
				C.cYellow + "Right-Click" + C.cGray + " with Axe to use " + C.cGreen + "Bacon Blast"
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

		org.bukkit.entity.Item ent = player.getWorld().dropItem(player.getEyeLocation().add(player.getLocation().getDirection().multiply(1.5)), ItemStackFactory.Instance.CreateStack(Material.PORKCHOP, (byte)0, 16));

		UtilAction.velocity(ent, player.getLocation().getDirection(), 1, false, 0, 0.2, 10, false);	

// Manager.getDamager().AddThrow(ent, player, this, -1, true, true, true,
// null, 1f, 1f,
// null, 1, UpdateType.SLOW,
// 0.5f);

		//Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));

		//Effect
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PIG_AMBIENT, 2f, 2f);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data) 
	{
		Explode(data);

		if (target == null)
			return;

		//Damage Event
		Manager.GetDamage().NewDamageEvent(target, data.GetThrower(), null,
				DamageCause.PROJECTILE, 6, true, true, false,
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
		data.GetThrown().getWorld().createExplosion(data.GetThrown().getLocation(), 0.5f);
		data.GetThrown().remove();
	}
}
