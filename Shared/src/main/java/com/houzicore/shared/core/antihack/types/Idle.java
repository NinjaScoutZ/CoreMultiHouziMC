package com.houzicore.shared.core.antihack.types;

import java.util.HashMap;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.core.antihack.AntiHack;
import com.houzicore.shared.core.antihack.Detector;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

public class Idle extends MiniPlugin implements Detector {
	private final AntiHack Host;

	private final HashMap<Player, Long> _idleTime = new HashMap<>();

	public Idle(AntiHack host) {
		super("Idle Detector", host.getPlugin());
		Host = host;
	}

	@Override
	public void Reset(Player player) {
		_idleTime.remove(player);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void updateFlyhack(PlayerMoveEvent event) {
		if (!Host.isEnabled())
			return;

		final Player player = event.getPlayer();

		_idleTime.put(player, System.currentTimeMillis());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void updateFreeCam(UpdateEvent event) {
		if (!Host.isEnabled())
			return;

		if (event.getType() != UpdateType.FAST)
			return;

		for (final Player player : UtilServer.getPlayers()) {
			// 100% Valid
			if (Host.isValid(player, true)) {
				continue;
			}

			if (!_idleTime.containsKey(player)) {
				continue;
			}

			if (!UtilTime.elapsed(_idleTime.get(player), Host.IdleTime)) {
				continue;
			}

			// Host.addSuspicion(player, "Lag / Fly (Idle)");
			// player.kickPlayer(C.cGold + "HouziCore " + C.cRed + "Anti-Cheat " + C.cWhite +
			// "Kicked for Lag / Fly (Idle)");

			UtilPlayer.message(player, C.cRed + C.Bold + com.houzicore.shared.core.common.BrandConfig.mainServerName() + " Anti-Cheat detected Lagging / Fly (Idle)");
			UtilPlayer.message(player, C.cRed + C.Bold + "You have been returned to Lobby.");
			Host.Portal.sendPlayerToServer(player, "Lobby");
		}
	}
}
