package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.ItemGadget;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class ItemMeteorSmash extends ItemGadget {

	private final java.util.HashSet<Player> _smashing = new java.util.HashSet<>();

	public ItemMeteorSmash(GadgetManager manager) {
		super(manager, "Meteor Smash",
				new String[] { C.cWhite + "Leap into the air and slam", C.cWhite + "down with fiery vengeance!" },
				-2, Material.MAGMA_CREAM, (byte) 0, 8000, 
				new Ammo("Smash Uses", "500 Coins", Material.MAGMA_CREAM, (byte) 0, new String[] { C.cWhite + "Launches a meteor smash." }, 500, 10));
	}

	@Override
	public void ActivateCustom(Player player) {
		// Launch upward and forward
		org.bukkit.util.Vector vec = player.getLocation().getDirection();
		vec.setY(0).normalize().multiply(1.5).setY(1.2);
		player.setVelocity(vec);

		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 0.5f);
		_smashing.add(player);
	}

	@EventHandler
	public void onUpdate(UpdateEvent event) {
		if (event.getType() != UpdateType.TICK) return;

		java.util.Iterator<Player> it = _smashing.iterator();
		while(it.hasNext()) {
			Player p = it.next();
			if (!p.isOnline()) { it.remove(); continue; }

			// Trailing flame while in the air
			UtilParticle.PlayParticle(ParticleType.FLAME, p.getLocation().add(0, 1, 0), 0.2f, 0.2f, 0.2f, 0.05f, 5, ViewDist.NORMAL, UtilServer.getPlayers());

			// Detect impact utilizing negative Y velocity and ground state
			if (p.getVelocity().getY() < -0.1 && p.isOnGround()) {
				// Landed! Cone effect explosion
				p.getWorld().playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 0.8f);

				for(int r = 1; r <= 3; r++) {
					for(int i = 0; i < 360; i += 20) {
						double rad = Math.toRadians(i);
						double x = Math.cos(rad) * r;
						double z = Math.sin(rad) * r;
						UtilParticle.PlayParticle(ParticleType.LAVA, p.getLocation().add(x, 0.2, z), 0f, 0f, 0f, 0f, 1, ViewDist.NORMAL, UtilServer.getPlayers());
						UtilParticle.PlayParticle(ParticleType.FLAME, p.getLocation().add(x, 0.2 + (3 - r)*0.5, z), 0f, 0.1f, 0f, 0.1f, 2, ViewDist.NORMAL, UtilServer.getPlayers());
					}
				}
				it.remove();
			}
		}
	}
}
