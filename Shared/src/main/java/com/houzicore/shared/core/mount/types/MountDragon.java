package com.houzicore.shared.core.mount.types;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.core.mount.DragonData;
import com.houzicore.shared.core.mount.DragonMount;
import com.houzicore.shared.core.mount.MountManager;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class MountDragon extends DragonMount {
	public MountDragon(MountManager manager) {
		super(manager, "Ethereal Dragon",
				new String[] { C.cWhite + "From the distant ether realm,", C.cWhite + "this prized dragon is said to",
						C.cWhite + "obey only true Heroes!", " ", C.cPurple + "Unlocked with Hero Rank", },
				Material.DRAGON_EGG, (byte) 0, -1);
	}

	@EventHandler
	public void DragonLocation(UpdateEvent event) {
		if (event.getType() != UpdateType.TICK)
			return;

		for (final DragonData data : GetActive().values()) {
			data.Move();
		}

		final HashSet<Player> toRemove = new HashSet<>();

		for (final Player player : GetActive().keySet()) {
			final DragonData data = GetActive().get(player);
			if (data == null) {
				toRemove.add(player);
				continue;
			}

			if (!data.DragonBase.isValid() || data.DragonBase.getPassenger() == null) {
				data.DragonBase.remove();
				toRemove.add(player);
				continue;
			}
		}

		for (final Player player : toRemove) {
			Disable(player);
		}
	}

	@EventHandler
	public void DragonTargetCancel(EntityTargetEvent event) {
		if (GetActive().containsValue(event.getEntity())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void HeroOwner(PlayerJoinEvent event) {
		if (Manager.getClientManager().Get(event.getPlayer()).GetRank().Has(Rank.SOVEREIGN)) {
			Manager.getDonationManager().Get(event.getPlayer().getName()).AddUnknownSalesPackagesOwned(GetName());
		}
	}

	public void setHealthPercent(double healthPercent) {
		for (final DragonData dragon : GetActive().values()) {
			double health = healthPercent * dragon.DragonBase.getMaxHealth();
			if (health <= 0.0) {
				health = 0.001;
			}
			dragon.DragonBase.setHealth(health);
		}
	}

	public void SetName(String news) {
		for (final DragonData dragon : GetActive().values()) {
			dragon.DragonBase.setCustomName(news);
		}
	}

	@EventHandler
	public void Trail(UpdateEvent event) {
		if (event.getType() == UpdateType.TICK) {
			for (final DragonData data : GetActive().values()) {
				UtilParticle.PlayParticle(ParticleType.WITCH_MAGIC, data.DragonBase.getLocation().add(0, 1, 0), 1f, 1f, 1f,
						0f, 20, ViewDist.NORMAL, UtilServer.getPlayers());
			}
		}
	}
}
