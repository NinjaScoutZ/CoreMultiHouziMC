package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.WinEffectGadget;

public class WinEffectLoveIsABattlefield extends WinEffectGadget {

	public WinEffectLoveIsABattlefield(GadgetManager manager) {
		super(manager, "Love is a Battlefield",
				new String[] { C.cWhite + "Don't hate the players.", C.cWhite + "Hate the game in a love room." }, -2,
				Material.RED_WOOL, (byte) 0, CosmeticRarity.LEGENDARY);
	}

	@Override
	public void playEffect(Player player) {
		pasteSchematic(player, "WinRoomLove");
		var base = getEffectLocation(player);

		base.getWorld().playSound(base, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);

		new BukkitRunnable() {
			int ticks = 0;

			@Override
			public void run() {
				if (ticks >= 40 || !player.isOnline()) {
					cancel();
					return;
				}

				double radius = 1.0;
				var center = base.clone().add(0, 1.5, 0);
				UtilParticle.playParticleRing(UtilParticle.ParticleType.HEART, center, radius, 8, UtilParticle.ViewDist.NORMAL);
				if (ticks % 8 == 0) {
					base.getWorld().playSound(base, Sound.ENTITY_CHICKEN_EGG, 0.7f, 1.3f);
				}
				ticks++;
			}
		}.runTaskTimer(Manager.getPlugin(), 0L, 2L);
	}
}
