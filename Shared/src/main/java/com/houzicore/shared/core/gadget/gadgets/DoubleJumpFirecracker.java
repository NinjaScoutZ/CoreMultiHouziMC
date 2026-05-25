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

public class DoubleJumpFirecracker extends DoubleJumpGadget {

	public DoubleJumpFirecracker(GadgetManager manager) {
		super(manager, "Firecracker Leap", new String[] { C.cWhite + "Launch yourself with a spark", C.cWhite + "and a crackling explosion." }, -2, Material.FIREWORK_ROCKET, (byte) 0);
	}

	@Override
	public void doDoubleJumpEffect(Player player) {
		player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_SHOOT, 1.0f, 1.0f);
		player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.2f);
		UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, player.getLocation().add(0, 0.5, 0), 0.5f, 0.5f, 0.5f, 0.15f, 30, ViewDist.NORMAL, UtilServer.getPlayers());
	}
}
