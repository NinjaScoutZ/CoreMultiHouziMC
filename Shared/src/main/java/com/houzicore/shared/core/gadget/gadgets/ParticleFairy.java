package com.houzicore.shared.core.gadget.gadgets;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.gadget.types.ParticleGadget;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.gadget.GadgetManager;

public class ParticleFairy extends ParticleGadget {
	private final HashMap<Player, ParticleFairyData> _fairy = new HashMap<>();

	public ParticleFairy(GadgetManager manager) {
		super(manager, "Flame Fairy",
				new String[] { C.cWhite + "HEY! LISTEN!", C.cWhite + "HEY! LISTEN!", C.cWhite + "HEY! LISTEN!", }, -2,
				Material.BLAZE_POWDER, (byte) 0);
	}

	private void clean(Player player) {
		_fairy.remove(player);
	}

	@Override
	public void DisableCustom(Player player) {
		if (_active.remove(player)) {
			UtilPlayer.message(player, F.main("Gadget", "You unsummoned " + F.elem(GetName()) + "."));
		}

		clean(player);
	}

	@EventHandler
	public void playParticle(UpdateEvent event) {
		if (event.getType() != UpdateType.TICK)
			return;

		// Launch
		for (final Player player : GetActive()) {
			if (!shouldDisplay(player)) {
				continue;
			}

			// Create
			if (!_fairy.containsKey(player)) {
				_fairy.put(player, new ParticleFairyData(player));
			}

			_fairy.get(player).Update();
		}
	}

	@EventHandler
	public void quit(PlayerQuitEvent event) {
		clean(event.getPlayer());
	}
}
