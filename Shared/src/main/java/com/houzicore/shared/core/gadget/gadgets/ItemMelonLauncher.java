package com.houzicore.shared.core.gadget.gadgets;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.Particle;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.ItemGadget;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.projectile.IThrown;
import com.houzicore.shared.core.projectile.ProjectileUser;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class ItemMelonLauncher extends ItemGadget implements IThrown {
	private final ArrayList<Item> _melon = new ArrayList<>();

	public ItemMelonLauncher(GadgetManager manager) {
		super(manager, "Melon Launcher",
				new String[] { C.cWhite + "Deliciously fun!", C.cWhite + "Eat the melon slices for a",
						C.cWhite + "temporary speed boost!", },
				-1, Material.MELON, (byte) 0, 1000, new Ammo("Melon Launcher", "100 Melons", Material.MELON,
						(byte) 0, new String[] { C.cWhite + "100 Melons for you to launch!" }, 500, 100));
	}

	@Override
	public void ActivateCustom(Player player) {
		// Action
		final Item item = player.getWorld().dropItem(player.getEyeLocation().add(player.getLocation().getDirection()),
				ItemStackFactory.Instance.CreateStack(Material.MELON));
		UtilAction.velocity(item, player.getLocation().getDirection(), 1, false, 0, 0.2, 10, false);

		Manager.getProjectileManager().AddThrow(item, player, this, -1, true, true, true, null, 1f, 1f, null, null, 0,
				UpdateType.TICK, 0.5f);

		// Inform
		boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
		UtilPlayer.message(player, F.main("Skill", isThai ? "\u00A77\u0e04\u0e38\u0e13\u0e43\u0e0a\u0e49 " + F.skill(GetName()) : "§7You used " + F.skill(GetName())));

		// Effect
		item.getWorld().playSound(item.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 0.5f);
	}

	@EventHandler
	public void cleanupMelon(UpdateEvent event) {
		if (event.getType() != UpdateType.SLOW)
			return;

		for (final Iterator<Item> melonIterator = _melon.iterator(); melonIterator.hasNext();) {
			final Item melon = melonIterator.next();

			if (melon.isDead() || !melon.isValid() || melon.getTicksLived() > 400) {
				melonIterator.remove();
				melon.remove();
			}
		}

		while (_melon.size() > 60) {
			final Item item = _melon.remove(0);
			item.remove();
		}
	}

	@Override
	public void Collide(LivingEntity target, Block block, ProjectileUser data) {
		if (target != null) {
			// Push
			UtilAction.velocity(target, UtilAlg.getTrajectory2d(data.GetThrown().getLocation(), target.getLocation()),
					1.4, false, 0, 0.8, 1.5, true);

			// Effect
			target.playEffect(EntityEffect.HURT);
		}

		smash(data.GetThrown());
	}

	@Override
	public void Expire(ProjectileUser data) {
		smash(data.GetThrown());
	}

	@Override
	public void Idle(ProjectileUser data) {
		smash(data.GetThrown());
	}

	@EventHandler
	public void pickupMelon(EntityPickupItemEvent event) {
		if (!_melon.remove(event.getItem()))
			return;

		event.getItem().remove();

		event.setCancelled(true);

		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1f, 1f);

			if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
				player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 80, 1), true);
			}
		}
	}

	public void smash(Entity ent) {
		// Effect
		ent.getWorld().spawnParticle(Particle.BLOCK, ent.getLocation().add(0, 0.5, 0), 10, 0.3, 0.3, 0.3, 0.1, Material.MELON.createBlockData());

		for (int i = 0; i < 10; i++) {
			final Item item = ent.getWorld().dropItem(ent.getLocation(),
					ItemStackFactory.Instance.CreateStack(Material.MELON));
			item.setVelocity(new Vector(UtilMath.rr(0.5, true), UtilMath.rr(0.5, false), UtilMath.rr(0.5, true)));
			item.setPickupDelay(30);

			_melon.add(item);
		}

		// Remove
		ent.remove();
	}
}
