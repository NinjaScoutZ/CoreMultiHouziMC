package com.houzicore.shared.core.gadget.gadgets.item;

import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.LineFormat;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilText;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.gadgets.Ammo;
import com.houzicore.shared.core.gadget.types.ItemGadget;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.projectile.IThrown;
import com.houzicore.shared.core.projectile.ProjectileUser;
import com.houzicore.shared.updater.UpdateType;

public class ItemFleshHook extends ItemGadget implements IThrown {
	public ItemFleshHook(GadgetManager manager) {
		super(manager, "Flesh Hook",
				new String[] {
						C.cWhite + "Make new friends by throwing a hook",
						C.cWhite + "into their face and pulling them towards you!"
				},
				-1, Material.TRIPWIRE_HOOK, (byte) 0, 2000,
				new Ammo("Flesh Hook", "50 Flesh Hooks", Material.TRIPWIRE_HOOK, (byte) 0,
						new String[] { C.cWhite + "50 Flesh Hooks for you to use!" }, 1000, 50));
	}

	@Override
	public void ActivateCustom(Player player) {
		// Action
		Item item = player.getWorld().dropItem(player.getEyeLocation().add(player.getLocation().getDirection()),
				ItemStackFactory.Instance.CreateStack(Material.TRIPWIRE_HOOK, (byte) 0, 1, "Flesh Hook"));
		UtilAction.velocity(item, player.getLocation().getDirection(), 1.6, false, 0, 0.2, 10, false);

		Manager.getProjectileManager().AddThrow(item, player, this, -1, true, true, true, Sound.ENTITY_BLAZE_SHOOT, 1.4f,
				0.8f, ParticleType.CRIT, UpdateType.TICK, 0.5f);

		// Inform
		UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));

		// Effect
		item.getWorld().playSound(item.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 2f, 0.8f);
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data) {
		if (data.GetThrown() != null) data.GetThrown().remove();

		if (!(data.GetThrower() instanceof Player))
			return;

		Player player = (Player) data.GetThrower();

		if (target == null || target.equals(player)) {
			return;
		}

		// Pull
		UtilAction.velocity(target, UtilAlg.getTrajectory(target.getLocation(), player.getLocation()), 3, false, 0, 0.8,
				1.5, true, player, GetName());

		// Effect
		target.playEffect(EntityEffect.HURT);

		// Inform
		UtilPlayer.message(target,
				F.main("Skill", F.name(player.getName()) + " hit you with " + F.skill(GetName()) + "."));
	}

	@Override
	public void Idle(ProjectileUser data) {
		if (data.GetThrown() != null) data.GetThrown().remove();
	}

	@Override
	public void Expire(ProjectileUser data) {
		if (data.GetThrown() != null) data.GetThrown().remove();
	}
}
