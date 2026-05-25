package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.ItemGadget;

public class ItemFreezeCannon extends ItemGadget {

	public ItemFreezeCannon(GadgetManager manager) {
		super(manager, "Freeze Cannon",
				new String[] { C.cWhite + "Blast a short cone of frost", C.cWhite + "to shove nearby players away." },
				-1, Material.SNOWBALL, (byte) 0, 4500,
				new Ammo("Freeze Cannon", "20 Frost Charges", Material.SNOWBALL, (byte) 0,
						new String[] { C.cWhite + "Extra freeze charges for your cannon." }, 1000, 20));
	}

	@Override
	public void ActivateCustom(Player player) {
		Vector direction = player.getEyeLocation().getDirection().normalize();
		var world = player.getWorld();
		var origin = player.getEyeLocation().clone();

		world.playSound(origin, Sound.ENTITY_SNOW_GOLEM_SHOOT, 1f, 0.9f);
		world.playSound(origin, Sound.BLOCK_GLASS_BREAK, 0.5f, 1.8f);

		for (int i = 1; i <= 12; i++) {
			var point = origin.clone().add(direction.clone().multiply(i * 0.5));
			world.spawnParticle(org.bukkit.Particle.SNOWFLAKE, point, 6, 0.15, 0.15, 0.15, 0.01);
			world.spawnParticle(org.bukkit.Particle.CLOUD, point, 1, 0.05, 0.05, 0.05, 0.0);
		}

		for (Player other : UtilServer.getPlayers()) {
			if (other.equals(player)) {
				continue;
			}

			Vector toOther = other.getLocation().toVector().subtract(origin.toVector());
			double distance = toOther.length();
			if (distance > 6.5 || distance < 0.2) {
				continue;
			}

			if (direction.dot(toOther.normalize()) < 0.72) {
				continue;
			}

			if (Manager.collideEvent(this, other)) {
				continue;
			}

			Vector push = other.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(0.8);
			push.setY(0.28);
			other.setVelocity(push);
			world.spawnParticle(org.bukkit.Particle.SNOWFLAKE, other.getLocation().add(0, 1, 0), 12, 0.25, 0.35, 0.25, 0.02);
			world.playSound(other.getLocation(), Sound.BLOCK_POWDER_SNOW_BREAK, 0.7f, 1.1f);
		}
	}
}
