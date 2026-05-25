package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.core.gadget.types.ParticleGadget;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.gadget.GadgetManager;

public class ParticleEnchant extends ParticleGadget {
	public ParticleEnchant(GadgetManager manager) {
		super(manager, "Enchanted",
				new String[] { C.cWhite + "The wisdom of the universe", C.cWhite + "suddenly finds you extremely",
						C.cWhite + "attractive, and wants to", C.cWhite + "\'enchant\' you.", },
				-2, Material.BOOK, (byte) 0);
	}

	@EventHandler
	public void playParticle(UpdateEvent event) {
		if (event.getType() != UpdateType.TICK)
			return;

		for (final Player player : GetActive()) {
			if (!shouldDisplay(player)) {
				continue;
			}

			if (Manager.isMoving(player)) {
				UtilParticle.PlayParticle(ParticleType.ENCHANTMENT_TABLE, player.getLocation().add(0, 1, 0), 0.2f, 0.2f,
						0.2f, 0, 4, ViewDist.NORMAL, UtilServer.getPlayers());
			} else {
				UtilParticle.PlayParticle(ParticleType.ENCHANTMENT_TABLE, player.getLocation().add(0, 1.4, 0), 0f, 0f,
						0f, 1, 4, ViewDist.NORMAL, UtilServer.getPlayers());
			}
		}
	}
}
