package com.houzicore.shared.core.gadget.gadgets;

import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ParticleFairyData {
	public Player Player;
	public Location Fairy;
	public Vector Direction;
	public Location Target;
	public double Speed;
	public long IdleTime;

	public ParticleFairyData(Player player) {
		Player = player;
		Direction = new Vector(1, 0, 0);
		Fairy = player.getEyeLocation();
		Target = getNewTarget();

		Speed = 0.2;

		IdleTime = 0;
	}

	private Location getNewTarget() {
		return Player.getEyeLocation().add(Math.random() * 6 - 3, Math.random() * 1.5, Math.random() * 6 - 3);
	}

	public void Update() {
		// Update Target
		if (UtilMath.offset(Player.getEyeLocation(), Target) > 3 || UtilMath.offset(Fairy, Target) < 1) {
			Target = getNewTarget();
		}

		// Pause?
		if (Math.random() > 0.98) {
			IdleTime = System.currentTimeMillis() + (long) (Math.random() * 3000);
		}

		// Speed
		if (UtilMath.offset(Player.getEyeLocation(), Fairy) < 3) {
			if (IdleTime > System.currentTimeMillis()) {
				Speed = Math.max(0, Speed - 0.005);
			} else {
				Speed = Math.min(0.15, Speed + 0.005);
			}
		} else {
			IdleTime = 0;

			Speed = Math.min(0.15 + UtilMath.offset(Player.getEyeLocation(), Fairy) * 0.05, Speed + 0.02);
		}

		// Modify Direction
		Direction.add(UtilAlg.getTrajectory(Fairy, Target).multiply(0.15));
		if (Direction.length() < 1) {
			Speed = Speed * Direction.length();
		}
		UtilAlg.Normalize(Direction);

		// Move
		if (UtilMath.offset(Fairy, Target) > 0.1) {
			Fairy.add(Direction.clone().multiply(Speed));
		}

		// Particle
		UtilParticle.PlayParticle(ParticleType.FLAME, Fairy, 0, 0, 0, 0, 1, ViewDist.NORMAL, UtilServer.getPlayers());
		UtilParticle.PlayParticle(ParticleType.LAVA, Fairy, 0, 0, 0, 0, 1, ViewDist.NORMAL, UtilServer.getPlayers());

		// Sound
		Fairy.getWorld().playSound(Fairy, Sound.ENTITY_CAT_PURREOW, 0.1f, 3f);
	}

}
