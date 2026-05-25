package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.ItemGadget;

public class ItemTrampoline extends ItemGadget {

	public ItemTrampoline(GadgetManager manager) {
		super(manager, "Pocket Trampoline",
				new String[] { C.cWhite + "Kick up a bouncy launch", C.cWhite + "for yourself and nearby players." },
				-1, Material.SLIME_BLOCK, (byte) 0, 5000,
				new Ammo("Pocket Trampoline", "10 Bounce Pads", Material.SLIME_BALL, (byte) 0,
						new String[] { C.cWhite + "Extra bounce pads for quick launches." }, 1100, 10));
	}

	@Override
	public void ActivateCustom(Player player) {
		var center = player.getLocation().clone();
		var world = player.getWorld();

		world.playSound(center, Sound.BLOCK_SLIME_BLOCK_FALL, 1f, 0.9f);
		world.playSound(center, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 0.5f, 1.6f);
		world.spawnParticle(org.bukkit.Particle.ITEM_SLIME, center.clone().add(0, 0.2, 0), 18, 0.45, 0.08, 0.45, 0.0);
		world.spawnParticle(org.bukkit.Particle.CLOUD, center.clone().add(0, 0.15, 0), 16, 0.45, 0.08, 0.45, 0.02);

		for (Player other : UtilServer.getPlayers()) {
			if (other.getWorld() != world || other.getLocation().distanceSquared(center) > 9.0) {
				continue;
			}

			Vector push = other.getLocation().toVector().subtract(center.toVector());
			if (push.lengthSquared() > 0.01) {
				push.normalize().multiply(0.28);
			}
			push.setY(1.0);
			other.setVelocity(push);
		}
	}
}
