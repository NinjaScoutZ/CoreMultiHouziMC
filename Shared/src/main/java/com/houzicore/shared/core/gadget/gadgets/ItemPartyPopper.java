package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilFirework;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.ItemGadget;

public class ItemPartyPopper extends ItemGadget {
	public ItemPartyPopper(GadgetManager manager) {
		super(manager, "Party Popper",
				new String[] { C.cWhite + "Pop a shower of confetti", C.cWhite + "and let everyone nearby know",
						C.cWhite + "the celebration has started." },
				-2, Material.FIREWORK_STAR, (byte) 0, 12000,
				new Ammo("Party Popper", "5 Party Poppers", Material.FIREWORK_STAR, (byte) 0,
						new String[] { C.cWhite + "Five quick celebration bursts", C.cWhite + "for your next big moment." },
						3500, 5));
	}

	@Override
	public void ActivateCustom(Player player) {
		var world = player.getWorld();
		var origin = player.getLocation().add(0, 1.2, 0);

		world.playSound(origin, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1.3f);
		world.playSound(origin, Sound.BLOCK_NOTE_BLOCK_CHIME, 0.7f, 1.8f);
		world.playSound(origin, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.4f, 2f);

		world.spawnParticle(org.bukkit.Particle.FIREWORK, origin, 28, 0.6, 0.35, 0.6, 0.02);
		world.spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, origin, 20, 0.75, 0.5, 0.75, 0.01);
		world.spawnParticle(org.bukkit.Particle.NOTE, origin, 10, 0.7, 0.45, 0.7, 1);

		UtilFirework.playFirework(origin,
				FireworkEffect.builder().with(FireworkEffect.Type.BURST).flicker(true).trail(true)
						.withColor(Color.FUCHSIA, Color.AQUA, Color.YELLOW).build());

		for (Player other : player.getWorld().getPlayers()) {
			if (other.getLocation().distanceSquared(player.getLocation()) > 8 * 8) {
				continue;
			}

			other.spawnParticle(org.bukkit.Particle.FIREWORK, other.getLocation().add(0, 1.2, 0), 6, 0.35, 0.2, 0.35,
					0.01);
		}
	}
}
