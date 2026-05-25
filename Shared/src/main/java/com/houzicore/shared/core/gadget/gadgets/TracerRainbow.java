package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitRunnable;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.TracerGadget;

public class TracerRainbow extends TracerGadget {

	private static final Color[] COLORS = new Color[] {
		Color.fromRGB(255, 0, 0),     // Red
		Color.fromRGB(255, 127, 0),   // Orange
		Color.fromRGB(255, 255, 0),   // Yellow
		Color.fromRGB(0, 255, 0),     // Green
		Color.fromRGB(0, 0, 255),     // Blue
		Color.fromRGB(75, 0, 130),    // Indigo
		Color.fromRGB(148, 0, 211)    // Violet
	};

	public TracerRainbow(GadgetManager manager) {
		super(manager, "Rainbow Tracer", new String[] { C.cWhite + "Leaves a trail of colors", C.cWhite + "behind your arrows." }, -2, Material.PEONY, (byte) 0);
	}

	@Override
	public void playTracer(final Projectile projectile) {
		new BukkitRunnable() {
			int tick = 0;
			@Override
			public void run() {
				if (!projectile.isValid() || projectile.isOnGround()) {
					this.cancel();
					return;
				}
				
				Color color = COLORS[tick % COLORS.length];
				Particle.DustOptions options = new Particle.DustOptions(color, 1.2f);
				
				for (Player player : UtilServer.getPlayers()) {
					if (UtilMath.offset(player.getLocation(), projectile.getLocation()) > 24)
						continue;
					player.spawnParticle(Particle.DUST, projectile.getLocation(), 1, 0f, 0f, 0f, 0, options);
				}
				tick++;
			}
		}.runTaskTimer(Manager.getPlugin(), 0L, 1L);
	}
}
