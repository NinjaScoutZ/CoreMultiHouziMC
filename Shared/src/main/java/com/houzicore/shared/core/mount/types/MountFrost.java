package com.houzicore.shared.core.mount.types;

import org.bukkit.Material;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Horse.Variant;
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

public class MountFrost extends HorseMount {

	@Override
	public void EnableCustom(org.bukkit.entity.Player player) {
		Horse horse = player.getWorld().spawn(player.getLocation(), Horse.class);
		horse.setTamed(true);
		horse.getInventory().setSaddle(new org.bukkit.inventory.ItemStack(Material.SADDLE));
		horse.setMaxHealth(40.0);
		horse.setHealth(40.0);
		horse.setJumpStrength(0.8);
		horse.setColor(Horse.Color.WHITE);
		horse.setStyle(Horse.Style.WHITE);
		
		com.houzicore.shared.common.util.UtilEnt.Vegetate(horse);
		horse.addPassenger(player);
		
		_active.put(player, horse);
	}

	public MountFrost(MountManager manager) {
		super(manager, "Glacial Steed",
				new String[] { C.cWhite + "Born in the North Pole,", C.cWhite + "it leaves a trail of frost",
						C.cWhite + "as it moves!", },
				Material.SNOWBALL, (byte) 0, 15000, null, null, null, 1, null);
	}

	@EventHandler
	public void Trail(UpdateEvent event) {
		if (event.getType() == UpdateType.TICK) {
			for (final org.bukkit.entity.AbstractHorse horse : GetActive().values()) {
				UtilParticle.PlayParticle(ParticleType.SNOW_SHOVEL, horse.getLocation().add(0, 1, 0), 0.25f, 0.25f,
						0.25f, 0.1f, 4, ViewDist.NORMAL, UtilServer.getPlayers());
			}
		}
	}
}
