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

public class DoubleJumpSlime extends DoubleJumpGadget {

	public DoubleJumpSlime(GadgetManager manager) {
		super(manager, "Slime Jump", new String[] { C.cWhite + "Boiingg!", C.cWhite + "Jump like a bouncy slime." }, -2, Material.SLIME_BALL, (byte) 0);
	}

	@Override
	public void doDoubleJumpEffect(Player player) {
		player.playSound(player.getLocation(), Sound.ENTITY_SLIME_JUMP, 1.0f, 1.0f);
		UtilParticle.PlayParticle(ParticleType.SLIME, player.getLocation(), 0.3f, 0.1f, 0.3f, 0.1f, 20, ViewDist.NORMAL, UtilServer.getPlayers());
	}
}
