package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.DoubleJumpGadget;

public class DoubleJumpRainbow extends DoubleJumpGadget {

	private static final Color[] COLORS = new Color[] {
		Color.fromRGB(255, 0, 0),     // Red
		Color.fromRGB(255, 127, 0),   // Orange
		Color.fromRGB(255, 255, 0),   // Yellow
		Color.fromRGB(0, 255, 0),     // Green
		Color.fromRGB(0, 0, 255),     // Blue
		Color.fromRGB(148, 0, 211)    // Violet
	};

	public DoubleJumpRainbow(GadgetManager manager) {
		super(manager, "Rainbow Leap", new String[] { C.cWhite + "Launch yourself with a beautiful", C.cWhite + "burst of rainbow colors." }, -2, Material.DANDELION, (byte) 0);
	}

	@Override
	public void doDoubleJumpEffect(Player player) {
		player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.5f, 1.2f);
		player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.5f);
		
		for (Color color : COLORS) {
			Particle.DustOptions options = new Particle.DustOptions(color, 1.3f);
			for (Player other : UtilServer.getPlayers()) {
				if (UtilMath.offset(other.getLocation(), player.getLocation()) > 24)
					continue;
				other.spawnParticle(Particle.DUST, player.getLocation().add(0, 0.2, 0), 6, 0.4f, 0.3f, 0.4f, 0.05f, options);
			}
		}
	}
}
