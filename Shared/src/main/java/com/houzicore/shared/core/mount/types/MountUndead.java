package com.houzicore.shared.core.mount.types;

import org.bukkit.Material;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.event.EventHandler;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.core.mount.HorseMount;
import com.houzicore.shared.core.mount.MountManager;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class MountUndead extends HorseMount {

	@Override
	public void EnableCustom(org.bukkit.entity.Player player) {
		SkeletonHorse horse = player.getWorld().spawn(player.getLocation(), SkeletonHorse.class);
		horse.setTamed(true);
		horse.getInventory().setSaddle(new org.bukkit.inventory.ItemStack(Material.SADDLE));
		horse.setMaxHealth(40.0);
		horse.setHealth(40.0);
		horse.setJumpStrength(0.8);
		
		com.houzicore.shared.common.util.UtilEnt.Vegetate(horse);
		horse.addPassenger(player);
		
		_active.put(player, horse);
	}


	public MountUndead(MountManager manager) {
		super(manager, "Infernal Horror",
				new String[] { C.cWhite + "The most ghastly horse in", C.cWhite + "existance, from the pits of",
						C.cWhite + "the Nether.", },
				Material.BONE, (byte) 0, 20000, null, null, null, 0.8, null);
	}

	@EventHandler
	public void Trail(UpdateEvent event) {
		if (event.getType() == UpdateType.TICK) {
			for (final org.bukkit.entity.AbstractHorse horse : GetActive().values()) {
				UtilParticle.PlayParticle(ParticleType.FLAME, horse.getLocation().add(0, 1, 0), 0.25f, 0.25f, 0.25f, 0,
						2, ViewDist.NORMAL, UtilServer.getPlayers());
			}
		}

		if (event.getType() == UpdateType.FAST) {
			for (final org.bukkit.entity.AbstractHorse horse : GetActive().values()) {
				UtilParticle.PlayParticle(ParticleType.LAVA, horse.getLocation().add(0, 1, 0), 0.25f, 0.25f, 0.25f, 0,
						1, ViewDist.NORMAL, UtilServer.getPlayers());
			}
		}

	}
}
