package com.houzicore.shared.core.gadget.gadgets;

import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.Sound;
////import org.bukkit.craftbukkit.v1_21_R1.entity.org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Bat;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.ItemGadget;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class ItemFootball extends ItemGadget {
	private final HashSet<Bat> _active = new HashSet<>();

	public ItemFootball(GadgetManager manager) {
		super(manager, "Football",
				new String[] { C.cWhite + "An amazing souvenier from the", C.cWhite + com.houzicore.shared.core.common.BrandConfig.mainServerName() + " World Cup in 2053!", },
				-1, Material.CLAY_BALL, (byte) 3, 1000, new Ammo("Melon Launcher", "10 Footballs", Material.CLAY_BALL,
						(byte) 0, new String[] { C.cWhite + "10 Footballs to play with" }, 1000, 10));
	}

	@Override
	public void ActivateCustom(Player player) {
		// Spawn football with no gravity/physics so it doesn't despawn instantly
		final FallingBlock ball = player.getWorld().spawnFallingBlock(player.getLocation().add(0, 1, 0), Material.PLAYER_HEAD.createBlockData());
		ball.setGravity(false);
		ball.setDropItem(false);

		final Bat bat = player.getWorld().spawn(player.getLocation(), Bat.class);
		UtilEnt.Vegetate(bat);
		UtilEnt.ghost(bat, true, true);
		UtilEnt.silence(bat, true);

		bat.addPassenger(ball);

		_active.add(bat);

		// Inform
		boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
		UtilPlayer.message(player, F.main("Skill", isThai ? "\u00A77\u0e04\u0e38\u0e13\u0e43\u0e0a\u0e49 " + F.skill(GetName()) : "§7You used " + F.skill(GetName())));
	}

	@EventHandler
	public void Collide(UpdateEvent event) {
		if (event.getType() != UpdateType.TICK)
			return;

		for (final Bat ball : _active) {
			if (!ball.getPassengers().isEmpty()) {
				// NMS removed: ((org.bukkit.entity.FallingBlock) ball.getPassenger()).getHandle().ticksLived = 1;
				// gravity is disabled, so no need to reset ticksLived
			}

			for (final Player other : UtilServer.getPlayers()) {
				if (UtilMath.offset(ball, other) > 1.5) {
					continue;
				}

				if (!Recharge.Instance.use(other, GetName() + " Bump", 200, false, false)) {
					continue;
				}

				double power = 0.4;
				if (other.isSprinting()) {
					power = 0.7;
				}

				// Velocity
				UtilAction.velocity(ball, UtilAlg.getTrajectory2d(other, ball), power, false, 0, 0, 0, false);

				other.getWorld().playSound(other.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.2f, 0.2f);
			}
		}
	}

	@EventHandler
	public void Snort(PlayerInteractEvent event) {
		final Player player = event.getPlayer();

		if (!UtilEvent.isAction(event, ActionType.L))
			return;

		for (final Bat ball : _active) {
			if (UtilMath.offset(ball, player) > 2) {
				continue;
			}

			if (!Recharge.Instance.use(player, GetName() + " Kick", 1000, false, false))
				return;

			Recharge.Instance.useForce(player, GetName() + " Bump", 1000);

			// Velocity
			UtilAction.velocity(ball, UtilAlg.getTrajectory2d(player, ball), 2, false, 0, 0, 0, false);

			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 0.1f);
		}
	}
}
