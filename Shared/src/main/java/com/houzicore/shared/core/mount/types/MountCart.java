package com.houzicore.shared.core.mount.types;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;

import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.mount.Mount;
import com.houzicore.shared.core.mount.MountManager;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class MountCart extends Mount<Minecart> {
	public MountCart(MountManager manager) {
		super(manager, "Minecart", Material.MINECART, (byte) 0, new String[] {
				ChatColor.RESET + "Cruise around town in your", ChatColor.RESET + "new Minecart VX Turbo!", }, 15000);

		KnownPackage = false;
	}

	@EventHandler
	public void cancelBreak(VehicleDamageEvent event) {
		if (GetActive().values().contains(event.getVehicle())) {
			event.setCancelled(true);
		}
	}

	@Override
	public void Disable(Player player) {
		final Minecart mount = _active.remove(player);
		if (mount != null) {
			mount.remove();

			// Inform
			UtilPlayer.message(player, F.main("Mount", "You despawned " + F.elem(GetName()) + "."));

			Manager.removeActive(player);
		}
	}

	@Override
	public void EnableCustom(Player player) {
		player.leaveVehicle();
		player.eject();

		// Remove other mounts
		Manager.DeregisterAll(player);

		final Minecart mount = player.getWorld().spawn(player.getLocation().add(0, 2, 0), Minecart.class);

		// Inform
		UtilPlayer.message(player, F.main("Mount", "You spawned " + F.elem(GetName()) + "."));

		// Store
		_active.put(player, mount);
	}

	@EventHandler
	public void interactMount(PlayerInteractEntityEvent event) {
		if (event.getRightClicked() == null)
			return;

		if (!GetActive().containsKey(event.getPlayer()))
			return;

		if (!GetActive().get(event.getPlayer()).equals(event.getRightClicked())) {
			UtilPlayer.message(event.getPlayer(), F.main("Mount", "This is not your Mount!"));
			return;
		}

		event.getPlayer().leaveVehicle();
		event.getPlayer().eject();

		event.getRightClicked().addPassenger(event.getPlayer());
	}

	@EventHandler
	public void target(EntityTargetEvent event) {
		if (!GetActive().containsKey(event.getTarget()))
			return;

		if (!GetActive().get(event.getTarget()).equals(event.getEntity())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void updateBounce(UpdateEvent event) {
		if (event.getType() != UpdateType.TICK)
			return;

			// Bounce
		for (final Minecart cart : GetActive().values()) {
			if (cart.getPassengers().isEmpty()) {
				continue;
			}

			if (!UtilEnt.isGrounded(cart)) {
				continue;
			}

			if (!(cart.getPassengers().get(0) instanceof Player)) {
				continue;
			}

			UtilAction.velocity(cart, cart.getPassengers().get(0).getLocation().getDirection(), 1.4, true, 0, 0, 1, false);

			if (Math.random() > 0.8) {
				cart.getWorld().playSound(cart.getLocation(), Sound.ENTITY_MINECART_RIDING, 0.05f, 2f);
			}
		}

		// Collide
		for (final Minecart cart : GetActive().values()) {
			if (cart.getPassengers().isEmpty()) {
				continue;
			}

			if (!(cart.getPassengers().get(0) instanceof Player)) {
				continue;
			}

			final Player player = (Player) cart.getPassengers().get(0);

			if (!Recharge.Instance.usable(player, GetName() + " Collide")) {
				continue;
			}

			for (final Minecart other : GetActive().values()) {
				if (other.equals(cart)) {
					continue;
				}

				if (other.getPassengers().isEmpty()) {
					continue;
				}

				if (!(other.getPassengers().get(0) instanceof Player)) {
					continue;
				}

				final Player otherPlayer = (Player) other.getPassengers().get(0);

				if (!Recharge.Instance.usable(otherPlayer, GetName() + " Collide")) {
					continue;
				}

				// Collide
				if (UtilMath.offset(cart, other) > 2) {
					continue;
				}

				Recharge.Instance.useForce(player, GetName() + " Collide", 500);
				Recharge.Instance.useForce(otherPlayer, GetName() + " Collide", 500);

				UtilAction.velocity(cart, UtilAlg.getTrajectory(other, cart), 1.2, false, 0, 0.8, 10, true);
				UtilAction.velocity(other, UtilAlg.getTrajectory(cart, other), 1.2, false, 0, 0.8, 10, true);

				cart.getWorld().playSound(cart.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 1f, 0.5f);
				other.getWorld().playSound(other.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 1f, 0.5f);

				// player.playEffect(EntityEffect.HURT);
				// otherPlayer.playEffect(EntityEffect.HURT);
			}
		}
	}
}
