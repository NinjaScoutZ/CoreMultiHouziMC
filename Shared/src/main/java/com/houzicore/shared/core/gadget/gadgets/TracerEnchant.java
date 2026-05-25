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

public class TracerEnchant extends TracerGadget {

	public TracerEnchant(GadgetManager manager) {
		super(manager, "Enchant Tracer", new String[] { C.cWhite + "Leaves a trail of magical runes", C.cWhite + "behind your arrows." }, -2, Material.ENCHANTING_TABLE, (byte) 0);
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
				
				UtilParticle.PlayParticle(ParticleType.ENCHANTMENT_TABLE, projectile.getLocation(), 0.1f, 0.1f, 0.1f, 0.1f, 4, ViewDist.NORMAL, UtilServer.getPlayers());
			}
		}.runTaskTimer(Manager.getPlugin(), 0L, 1L);
	}
}
