package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.DoubleJumpGadget;

public class DoubleJumpCupidsWings extends DoubleJumpGadget {

	public DoubleJumpCupidsWings(GadgetManager manager) {
		super(manager, "Cupid's Wings", new String[] { C.cWhite + "Fly with the grace of love,", C.cWhite + "leaving a burst of hearts." }, -2, Material.FEATHER, (byte) 0);
	}

	@Override
	public void doDoubleJumpEffect(Player player) {
		player.playSound(player.getLocation(), Sound.ENTITY_BAT_LOOP, 1.5f, 1.2f);
		player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.5f);
		UtilParticle.PlayParticle(ParticleType.HEART, player.getLocation().add(0, 0.5, 0), 0.4f, 0.4f, 0.4f, 0.1f, 12, ViewDist.NORMAL, UtilServer.getPlayers());
	}
}
