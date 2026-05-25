package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.ItemGadget;

public class ItemSnowballVolley extends ItemGadget {
	public ItemSnowballVolley(GadgetManager manager) {
		super(manager, "Snowball Volley",
				new String[] { C.cWhite + "Launch a wide spray of snowballs", C.cWhite + "to make the lobby feel",
						C.cWhite + "a little more chaotic." },
				-2, Material.SNOWBALL, (byte) 0, 9000,
				new Ammo("Snowball Volley", "16 Snowball Volleys", Material.SNOWBALL, (byte) 0,
						new String[] { C.cWhite + "Sixteen bursts of harmless snowballs." }, 3000, 16));
	}

	@Override
	public void ActivateCustom(Player player) {
		var world = player.getWorld();
		var eye = player.getEyeLocation();

		world.playSound(eye, Sound.ENTITY_SNOW_GOLEM_SHOOT, 1f, 1.15f);
		world.playSound(eye, Sound.BLOCK_POWDER_SNOW_BREAK, 0.8f, 1.2f);
		world.spawnParticle(org.bukkit.Particle.SNOWFLAKE, eye, 18, 0.2, 0.2, 0.2, 0.01);

		for (int i = -2; i <= 2; i++) {
			Snowball snowball = player.launchProjectile(Snowball.class);
			Vector velocity = player.getLocation().getDirection().clone();
			velocity.add(new Vector(i * 0.08, (i == 0 ? 0.01 : 0.03), 0));
			snowball.setVelocity(velocity.multiply(1.2));
			snowball.setBounce(false);
		}
	}
}
