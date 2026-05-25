package com.houzicore.shared.core.mount.types;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.Player;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.core.mount.HorseMount;
import com.houzicore.shared.core.mount.MountManager;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class MountNightmareSteed extends HorseMount {

	private boolean _foot = false;
	private final Map<Location, Long> _steps = new HashMap<>();

	public MountNightmareSteed(MountManager manager) {
		super(manager, "Nightmare Steed",
				new String[] {
						C.cGray + "The Nightmare Steed comes in the darkness",
						C.cGray + "of night, the fires of the underworld",
						C.cGray + "still trailing from its hooves."
				}, Material.BLACK_WOOL, (byte) 15, -1, Color.BLACK, Style.NONE, null, 1.0, null);
	}

	@Override
	public void EnableCustom(Player player) {
		SkeletonHorse horse = player.getWorld().spawn(player.getLocation(), SkeletonHorse.class);
		horse.setTamed(true);
		horse.getInventory().setSaddle(new org.bukkit.inventory.ItemStack(Material.SADDLE));
		horse.setMaxHealth(40.0);
		horse.setHealth(40.0);
		horse.setJumpStrength(0.8);
		
		UtilEnt.Vegetate(horse);
		horse.addPassenger(player);
		
		_active.put(player, horse);
	}

	@EventHandler
	public void onUpdate(UpdateEvent event) {
		if (event.getType() == UpdateType.SEC) {
			for (AbstractHorse horse : GetActive().values()) {
				UtilParticle.PlayParticleToAll(ParticleType.LARGE_SMOKE, horse.getLocation(), 0f, 0f,
						0f, 0.05f, 5, ViewDist.NORMAL);
			}
		}

		if (event.getType() != UpdateType.FASTEST) {
			return;
		}

		for (AbstractHorse horse : GetActive().values()) {
			_foot = !_foot;

			cleanSteps();

			if (!UtilEnt.isGrounded(horse)) {
				return;
			}

			Vector offset;
			Vector dir = horse.getLocation().getDirection();
			dir.setY(0);
			dir.normalize();

			if (_foot) {
				offset = new Vector(dir.getZ() * -1, 0.1, dir.getX());
			} else {
				offset = new Vector(dir.getZ(), 0.1, dir.getX() * -1);
			}

			Location loc = horse.getLocation().add(offset.multiply(0.2));

			if (nearStep(loc)) {
				return;
			}
			if (!UtilBlock.solid(loc.getBlock().getRelative(BlockFace.DOWN))) {
				return;
			}

			_steps.put(loc, System.currentTimeMillis());

			UtilParticle.PlayParticleToAll(ParticleType.FLAME, loc, 0f, 0f, 0f, 0.01f, 3, ViewDist.NORMAL);

			for (int i = 0; i < 5; i++) {
				Location randLoc = horse.getLocation().clone().add(Math.random() * 2 - 1, 1, Math.random() * 2 - 1);
				UtilParticle.PlayParticleToAll(ParticleType.FLAME, randLoc, 0f, 0f, 0f, 0.02f, 1, ViewDist.NORMAL);
				UtilParticle.PlayParticleToAll(ParticleType.LARGE_SMOKE, randLoc, 0f, 0f, 0f, 0.01f, 1, ViewDist.NORMAL);
			}
		}
	}

	private void cleanSteps() {
		_steps.entrySet().removeIf(entry -> UtilTime.elapsed(entry.getValue(), 10000));
	}

	private boolean nearStep(Location loc) {
		for (Location other : _steps.keySet()) {
			if (UtilMath.offset(loc, other) < 0.3) {
				return true;
			}
		}

		return false;
	}

}
