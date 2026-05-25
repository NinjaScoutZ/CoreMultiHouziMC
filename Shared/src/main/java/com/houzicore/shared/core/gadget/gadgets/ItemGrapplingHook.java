package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.ItemGadget;

public class ItemGrapplingHook extends ItemGadget {

	public ItemGrapplingHook(GadgetManager manager) {
		super(manager, "Grappling Hook",
				new String[] { C.cWhite + "Lunge through the lobby", C.cWhite + "with a quick forward grapple." }, -1,
				Material.FISHING_ROD, (byte) 0, 3500,
				new Ammo("Grappling Hook", "16 Hook Charges", Material.STRING, (byte) 0,
						new String[] { C.cWhite + "Hook charges for your next swing." }, 900, 16));
	}

	@Override
	public void ActivateCustom(Player player) {
		Vector direction = player.getEyeLocation().getDirection().normalize();
		Block target = player.getTargetBlockExact(18);
		Vector velocity = direction.clone().multiply(1.05).setY(Math.max(0.45, direction.getY() + 0.42));

		if (target != null) {
			Vector towards = target.getLocation().add(0.5, 0.5, 0.5).toVector().subtract(player.getLocation().toVector()).normalize();
			velocity = towards.multiply(1.15);
			velocity.setY(Math.max(0.55, velocity.getY() + 0.3));
		}

		player.setVelocity(velocity);
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FISHING_BOBBER_THROW, 1f, 1.05f);
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WIND_CHARGE_WIND_BURST, 0.5f, 1.5f);

		for (int i = 1; i <= 10; i++) {
			var point = player.getEyeLocation().clone().add(direction.clone().multiply(i * 0.6));
			player.getWorld().spawnParticle(org.bukkit.Particle.END_ROD, point, 1, 0.02, 0.02, 0.02, 0.0);
		}
	}
}
