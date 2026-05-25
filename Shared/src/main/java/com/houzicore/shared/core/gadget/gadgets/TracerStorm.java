package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Material;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitRunnable;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.TracerGadget;

public class TracerStorm extends TracerGadget {

	public TracerStorm(GadgetManager manager) {
		super(manager, "Storm Tracer", new String[] { C.cWhite + "Leaves a trail of storm clouds", C.cWhite + "and lightning sparks." }, -2, Material.CHARCOAL, (byte) 0);
	}

	@Override
	public void playTracer(final Projectile projectile) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (!projectile.isValid() || projectile.isOnGround()) {
					this.cancel();
					return;
				}
				
				UtilParticle.PlayParticle(ParticleType.CLOUD, projectile.getLocation(), 0.05f, 0.05f, 0.05f, 0.01f, 1, ViewDist.NORMAL, UtilServer.getPlayers());
				UtilParticle.PlayParticle(ParticleType.FIREWORKS_SPARK, projectile.getLocation(), 0.05f, 0.05f, 0.05f, 0.01f, 1, ViewDist.NORMAL, UtilServer.getPlayers());
			}
		}.runTaskTimer(Manager.getPlugin(), 0L, 1L);
	}
}
