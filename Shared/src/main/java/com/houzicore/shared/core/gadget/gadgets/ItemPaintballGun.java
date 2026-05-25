package com.houzicore.shared.core.gadget.gadgets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.event.GadgetBlockEvent;
import com.houzicore.shared.core.gadget.types.ItemGadget;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class ItemPaintballGun extends ItemGadget {
	private final HashSet<Projectile> _balls = new HashSet<>();

	public ItemPaintballGun(GadgetManager manager) {
		super(manager, "Paintball Gun", new String[] { C.cWhite + "PEW PEW PEW PEW!", }, 6000, Material.GOLDEN_HORSE_ARMOR,
				(byte) 0, 200, new Ammo("Paintball Gun", "100 Paintballs", Material.GOLDEN_HORSE_ARMOR, (byte) 0,
						new String[] { C.cWhite + "100 Paintballs for you to shoot!" }, 500, 100));
	}

	@Override
	public void ActivateCustom(Player player) {
		final Projectile proj = player.launchProjectile(EnderPearl.class);
		proj.setVelocity(proj.getVelocity().multiply(2));
		_balls.add(proj);

		// Sound
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1.5f, 1.2f);
	}

	@EventHandler
	public void cleanupBalls(UpdateEvent event) {
		if (event.getType() != UpdateType.SLOW)
			return;

		for (final Iterator<Projectile> ballIterator = _balls.iterator(); ballIterator.hasNext();) {
			final Projectile ball = ballIterator.next();

			if (ball.isDead() || !ball.isValid()) {
				ballIterator.remove();
			}
		}
	}

	@EventHandler
	public void Paint(ProjectileHitEvent event) {
		if (!_balls.remove(event.getEntity()))
			return;

		final Location loc = event.getEntity().getLocation().add(event.getEntity().getVelocity());
		loc.getWorld().spawnParticle(Particle.BLOCK, loc, 12, 0.3, 0.3, 0.3, 0.1, Material.PINK_WOOL.createBlockData());

		byte color = 2;
		final double r = Math.random();
		if (r > 0.8) {
			color = 4;
		} else if (r > 0.6) {
			color = 5;
		} else if (r > 0.4) {
			color = 9;
		} else if (r > 0.2) {
			color = 14;
		}

		for (final Block block : UtilBlock.getInRadius(loc, 3d).keySet()) {
			if (block.getType() == Material.NETHER_PORTAL)
				return;

			if (block.getType() == Material.CACTUS)
				return;

			if (block.getType() == Material.SUGAR_CANE)
				return;
		}

		final List<Block> blocks = new ArrayList<>();
		blocks.addAll(UtilBlock.getInRadius(loc, 1.5d).keySet());

		final GadgetBlockEvent gadgetEvent = new GadgetBlockEvent(this, blocks);
		Bukkit.getServer().getPluginManager().callEvent(gadgetEvent);

		if (gadgetEvent.isCancelled())
			return;

		for (final Block block : gadgetEvent.getBlocks()) {
			if (!UtilBlock.solid(block)) {
				continue;
			}

			if (block.getType() == Material.WHITE_CARPET) {
				Manager.getBlockRestore().Add(block, 171, color, 4000);
			} else {
				Manager.getBlockRestore().Add(block, 35, color, 4000);
			}
		}
	}

	@EventHandler
	public void Teleport(PlayerTeleportEvent event) {
		if (event.getCause() == TeleportCause.ENDER_PEARL) {
			event.setCancelled(true);
		}
	}
}
