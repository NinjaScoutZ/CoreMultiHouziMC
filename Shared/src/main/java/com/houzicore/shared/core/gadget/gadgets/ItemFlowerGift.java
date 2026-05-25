package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.ItemGadget;

public class ItemFlowerGift extends ItemGadget {
	public ItemFlowerGift(GadgetManager manager) {
		super(manager, "Flower Gift",
				new String[] { C.cWhite + "Share a bright little moment", C.cWhite + "with the closest player",
						C.cWhite + "or bloom it around yourself." },
				-2, Material.POPPY, (byte) 0, 8000,
				new Ammo("Flower Gift", "8 Flower Gifts", Material.POPPY, (byte) 0,
						new String[] { C.cWhite + "A pocketful of tiny celebrations." }, 2500, 8));
	}

	@Override
	public void ActivateCustom(Player player) {
		Player target = null;
		double bestDistance = 6 * 6;

		for (Player other : player.getWorld().getPlayers()) {
			if (other.equals(player)) {
				continue;
			}

			double distance = other.getLocation().distanceSquared(player.getLocation());
			if (distance < bestDistance) {
				bestDistance = distance;
				target = other;
			}
		}

		Player focus = target == null ? player : target;
		var bloom = focus.getLocation().add(0, 1.1, 0);

		focus.getWorld().spawnParticle(org.bukkit.Particle.HEART, bloom, 8, 0.45, 0.35, 0.45, 0.01);
		focus.getWorld().spawnParticle(org.bukkit.Particle.HAPPY_VILLAGER, bloom, 14, 0.55, 0.4, 0.55, 0.02);
		focus.getWorld().playSound(bloom, Sound.ENTITY_ALLAY_AMBIENT_WITH_ITEM, 0.9f, 1.4f);
		focus.getWorld().playSound(bloom, Sound.BLOCK_CHERRY_LEAVES_PLACE, 0.8f, 1.2f);

		if (target != null) {
			UtilPlayer.message(player, F.main("Gadget", "You gifted " + F.name(target.getName()) + " a flower burst."));
			UtilPlayer.message(target, F.main("Gadget", F.name(player.getName()) + " sent you a flower burst."));
		} else {
			UtilPlayer.message(player, F.main("Gadget", "You bloomed a flower burst around yourself."));
		}
	}
}
