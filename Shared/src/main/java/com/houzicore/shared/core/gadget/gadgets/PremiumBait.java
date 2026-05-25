package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.FishHook;

import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;

import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.BaitGadget;

public class PremiumBait extends BaitGadget {

	public PremiumBait(GadgetManager manager) {
		super(manager, "Premium Bait", new String[] {
				"§7Equip this bait to guarantee a",
				"§7100% bite chance when fishing!",
				"",
				"§eEach successful catch consumes 1 Ammo."
		}, -1, Material.TROPICAL_FISH, (byte) 0, new Ammo("Premium Bait", "50 Premium Baits", Material.TROPICAL_FISH, (byte)0, new String[] {
				"§7Adds 50 Premium Baits to your inventory."
		}, 500, 50));
	}

	@Override
	public void playBobberEffect(FishHook hook) {
		// Play heart particles around the bobber to indicate Premium status!
		if (hook != null && hook.isValid()) {
			UtilParticle.PlayParticle(ParticleType.HAPPY_VILLAGER, hook.getLocation().add(0, 0.4, 0), 0.1f, 0.1f, 0.1f, 0f, 1, ViewDist.SHORT, com.houzicore.shared.common.util.UtilServer.getPlayers());
		}
	}
}
