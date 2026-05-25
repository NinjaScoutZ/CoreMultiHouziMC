package com.houzicore.shared.core.visibility;

import java.util.Iterator;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.timing.TimingManager;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class VisibilityManager extends MiniPlugin {
	public static VisibilityManager Instance;

	public static void Initialize(JavaPlugin plugin) {
		Instance = new VisibilityManager(plugin);
	}

	private final NautHashMap<Player, VisibilityData> _data = new NautHashMap<>();

	protected VisibilityManager(JavaPlugin plugin) {
		super("Visibility Manager", plugin);
	}

	public VisibilityData getDataFor(Player player) {
		return _data.computeIfAbsent(player, k -> new VisibilityData());
	}

	@EventHandler
	public void quit(PlayerQuitEvent event) {
		_data.remove(event.getPlayer());
	}

	public void refreshPlayerToAll(Player player) {
		setVisibility(player, false, UtilServer.getPlayers());
		setVisibility(player, true, UtilServer.getPlayers());
	}

	public void setVisibility(Player target, boolean isVisible, Player... viewers) {
		TimingManager.startTotal("VisMan SetVis");

		for (final Player player : viewers) {
			if (player.equals(target)) {
				continue;
			}

			getDataFor(player).updatePlayer(player, target, !isVisible);
		}

		TimingManager.stopTotal("VisMan SetVis");
	}

	@EventHandler
	public void update(UpdateEvent event) {
		if (event.getType() != UpdateType.TICK)
			return;

		TimingManager.startTotal("VisMan Update");

		final Iterator<Player> playerIter = _data.keySet().iterator();

		while (playerIter.hasNext()) {
			final Player player = playerIter.next();

			if (!player.isOnline() || !player.isValid()) {
				playerIter.remove();
				continue;
			}

			_data.get(player).attemptToProcessUpdate(player);
		}

		TimingManager.stopTotal("VisMan Update");
	}

	// @EventHandler DISABLED
	public void updateDebug(UpdateEvent event) {
		if (event.getType() != UpdateType.MIN_01)
			return;

		TimingManager.endTotal("VisMan update", true);
		TimingManager.endTotal("VisMan setVis", true);
		TimingManager.endTotal("VisData attemptToProcess", true);
		TimingManager.endTotal("VisData updatePlayer", true);
		TimingManager.endTotal("VisData attemptToProcessUpdate shouldHide", true);
		TimingManager.endTotal("VisData attemptToProcessUpdate lastState", true);
		TimingManager.endTotal("Hide Player", true);
		TimingManager.endTotal("Show Player", true);
	}
}
