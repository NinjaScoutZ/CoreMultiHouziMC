package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.core.disguise.disguises.DisguiseBlaze;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.MorphGadget;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class MorphBlaze extends MorphGadget {
	public MorphBlaze(GadgetManager manager) {
		super(manager, "Blaze Morph",
				new String[] { C.cWhite + "Transforms the wearer into a fiery Blaze!", " ",
						C.cYellow + "Crouch" + C.cGray + " to use " + C.cGreen + "Firefly", " ",
						C.cPurple + "Unlocked with Hero Rank", },
				-1, Material.BLAZE_POWDER, (byte) 0);
	}

	@Override
	public void DisableCustom(Player player) {
		RemoveArmor(player);
		Manager.getDisguiseManager().undisguise(player);
	}

	@Override
	public void EnableCustom(final Player player) {
		ApplyArmor(player);

		final DisguiseBlaze disguise = new DisguiseBlaze(player);
		//disguise.setName(player.getName(), Manager.getClientManager().Get(player).GetRank());
		//disguise.setCustomNameVisible(true);
		Manager.getDisguiseManager().disguise(disguise);
	}

	@EventHandler
	public void HeroOwner(PlayerJoinEvent event) {
		if (Manager.getClientManager().Get(event.getPlayer()).GetRank().Has(Rank.SOVEREIGN)) {
			Manager.getDonationManager().Get(event.getPlayer().getName()).AddUnknownSalesPackagesOwned(GetName());
		}
	}

	@EventHandler
	public void Trail(UpdateEvent event) {
		if (event.getType() == UpdateType.TICK) {
			for (final Player player : GetActive()) {
				if (player.isSneaking()) {
					player.leaveVehicle();
					player.eject();

					player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 0.2f, (float) Math.random());
					UtilParticle.PlayParticle(ParticleType.FLAME, player.getLocation().add(0, 1, 0), 0.25f, 0.25f,
							0.25f, 0f, 3, ViewDist.NORMAL, UtilServer.getPlayers());
					UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, player.getLocation().add(0, 1, 0), 0.1f, 0.1f,
							0.1f, 0f, 1, ViewDist.NORMAL, UtilServer.getPlayers());
					UtilAction.velocity(player, 0.8, 0.1, 1, true);
				}
			}
		}
	}
}
