package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Particle;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.gadget.types.ParticleGadget;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.gadget.GadgetManager;

public class ParticleLegend extends ParticleGadget {
	public ParticleLegend(GadgetManager manager) {
		super(manager, "Legendary Aura", new String[] { C.cWhite + "These mystic particle attach to",
				C.cWhite + "only the most legendary of players!", " ", C.cPurple + "Unlocked with Legend Rank", }, -2,
				Material.END_PORTAL_FRAME, (byte) 0);
	}

	@EventHandler
	public void legendOwner(PlayerJoinEvent event) {
		if (Manager.getClientManager().Get(event.getPlayer()).GetRank().Has(Rank.DIVINE)) {
			Manager.getDonationManager().Get(event.getPlayer().getName()).AddUnknownSalesPackagesOwned(GetName());
		}
	}

	@EventHandler
	public void playParticle(UpdateEvent event) {
		if (event.getType() != UpdateType.TICK)
			return;

		for (final Player player : GetActive()) {
			if (!shouldDisplay(player)) {
				continue;
			}

			player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0), 3, 0.3, 0.3, 0.3, 0.02);
		}
	}
}
