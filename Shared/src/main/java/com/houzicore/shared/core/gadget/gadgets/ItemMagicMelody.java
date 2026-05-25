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

public class ItemMagicMelody extends ItemGadget {

	private final java.util.HashMap<Player, Integer> _activeMelodies = new java.util.HashMap<>();

	public ItemMagicMelody(GadgetManager manager) {
		super(manager, "Magic Melody",
				new String[] { C.cWhite + "Play an enchanting spherical", C.cWhite + "melody that heals the soul." },
				-2, Material.MUSIC_DISC_MALL, (byte) 0, 5000, 
				new Ammo("Melody Uses", "200 Coins", Material.MUSIC_DISC_MALL, (byte) 0, new String[] { C.cWhite + "Plays a magical tune." }, 200, 10));
	}

	@Override
	public void ActivateCustom(Player player) {
		player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1f, 1f);
		_activeMelodies.put(player, 0);
	}

	@EventHandler
	public void onUpdate(UpdateEvent event) {
		if (event.getType() != UpdateType.TICK) return;

		java.util.Iterator<Player> iterator = _activeMelodies.keySet().iterator();
		while (iterator.hasNext()) {
			Player player = iterator.next();
			if (!player.isOnline()) {
				iterator.remove();
				continue;
			}

			int ticks = _activeMelodies.get(player);
			if (ticks > 50) {
				iterator.remove();
				continue;
			}

			// Helix Double-Spiral formula
			double r = 0.5 + (ticks * 0.05); // Expanding radius
			for(int i = 0; i < 2; i++) {
				double offset = i * Math.PI;
				double x = Math.cos(ticks * 0.4 + offset) * r;
				double z = Math.sin(ticks * 0.4 + offset) * r;
				double y = ticks * 0.06;
				
				// Using math random on colors to yield multi-colored notes
				UtilParticle.PlayParticle(ParticleType.NOTE, player.getLocation().add(x, 0.5 + y, z), 
						(float)Math.random(), 0f, (float)Math.random(), 1f, 1, ViewDist.NORMAL, UtilServer.getPlayers());
			}

			// Arpeggio sound
			if (ticks % 5 == 0) {
				player.getWorld().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.8f, 0.5f + (ticks/50f));
			}

			_activeMelodies.put(player, ticks + 1);
		}
	}
}
