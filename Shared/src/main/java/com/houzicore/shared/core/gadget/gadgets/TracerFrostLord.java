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

public class TracerFrostLord extends TracerGadget {

	public TracerFrostLord(GadgetManager manager) {
		super(manager, "Frost Lord Tracer", new String[] { C.cWhite + "Leaves a trail of snow and ice", C.cWhite + "behind your arrows." }, -2, Material.ICE, (byte) 0);
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
				
				UtilParticle.PlayParticle(ParticleType.SNOW_SHOVEL, projectile.getLocation(), 0.1f, 0.1f, 0.1f, 0.01f, 2, ViewDist.NORMAL, UtilServer.getPlayers());
			}
		}.runTaskTimer(Manager.getPlugin(), 0L, 1L);
	}
}
