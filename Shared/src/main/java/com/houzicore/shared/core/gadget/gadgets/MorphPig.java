package com.houzicore.shared.core.gadget.gadgets;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.*;

import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.disguise.disguises.DisguisePig;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.MorphGadget;

public class MorphPig extends MorphGadget {
	private final HashSet<Player> _double = new HashSet<>();

	public MorphPig(GadgetManager manager) {
		super(manager, "Pig Morph",
				new String[] { C.cWhite + "Oink. Oink. Oink.... Oink?", " ",
						C.cYellow + "Left Click" + C.cGray + " to use " + C.cGreen + "Oink",
						C.cYellow + "Collide" + C.cGray + " to use " + C.cGreen + "Pig Bounce", " ",
						C.cPurple + "Unlocked with Ultra Rank", },
				-1, Material.PORKCHOP, (byte) 0);
	}

	@EventHandler
	public void Clean(PlayerQuitEvent event) {
		_double.remove(event.getPlayer());
	}

	@EventHandler
	public void Collide(PlayerToggleFlightEvent event) {
		_double.add(event.getPlayer());
		Recharge.Instance.useForce(event.getPlayer(), GetName() + " Double Jump", 200);
	}

	@EventHandler
	public void Collide(UpdateEvent event) {
		if (event.getType() != UpdateType.TICK)
			return;

		for (final Player player : GetActive()) {
			// Grounded
			if (_double.contains(player))
				if (UtilEnt.isGrounded(player))
					if (Recharge.Instance.usable(player, GetName() + " Double Jump")) {
						_double.remove(player);
					}

			double range = 1;

			if (_double.contains(player)) {
				range += 0.5;
			}

			if (player.getVehicle() != null) {
				continue;
			}

			if (!Recharge.Instance.usable(player, GetName() + " Collide")) {
				continue;
			}

			for (final Player other : UtilServer.getPlayers()) {
				if (other.equals(player)) {
					continue;
				}

				if (other.getVehicle() != null) {
					continue;
				}

				if (!Recharge.Instance.usable(other, GetName() + " Collide")) {
					continue;
				}

				if (UtilMath.offset(player, other) > range) {
					continue;
				}

				if (Manager.collideEvent(this, other)) {
					continue;
				}

				// Cooldown
				Recharge.Instance.useForce(other, GetName() + " Collide", 200);
				Recharge.Instance.useForce(player, GetName() + " Collide", 200);

				double power = 0.4;
				double height = 0.1;
				if (player.isSprinting()) {
					power = 0.6;
					height = 0.2;
				}

				if (_double.contains(player)) {
					power = 1;
					height = 0.3;
				}

				// Velocity
				UtilAction.velocity(player, UtilAlg.getTrajectory2d(other, player), power, false, 0, height, 1, true);
				UtilAction.velocity(other, UtilAlg.getTrajectory2d(player, other), power, false, 0, height, 1, true);

				// Sound
				if (_double.contains(player)) {
					player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PIG_DEATH,
							(float) (0.8 + Math.random() * 0.4), (float) (0.8 + Math.random() * 0.4));
				} else {
					player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PIG_AMBIENT, 1f,
							(float) (1.5 + Math.random() * 0.5));
				}
			}
		}
	}

	@Override
	public void DisableCustom(Player player) {
		RemoveArmor(player);
		Manager.getDisguiseManager().undisguise(player);
	}

	@Override
	public void EnableCustom(final Player player) {
		ApplyArmor(player);

		final DisguisePig disguise = new DisguisePig(player);
		//disguise.setName(player.getName(), Manager.getClientManager().Get(player).GetRank());
		//disguise.setCustomNameVisible(true);
		Manager.getDisguiseManager().disguise(disguise);
	}

	@EventHandler
	public void HeroOwner(PlayerJoinEvent event) {
		if (Manager.getClientManager().Get(event.getPlayer()).GetRank().Has(Rank.WARRIOR)) {
			Manager.getDonationManager().Get(event.getPlayer().getName()).AddUnknownSalesPackagesOwned(GetName());
		}
	}

	@EventHandler
	public void Snort(PlayerInteractEvent event) {
		final Player player = event.getPlayer();

		if (!IsActive(player))
			return;

		if (!UtilEvent.isAction(event, ActionType.L))
			return;

		if (!Recharge.Instance.use(player, GetName(), 400, false, false))
			return;

		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PIG_AMBIENT, 1f, (float) (0.75 + Math.random() * 0.5));

	}
}
