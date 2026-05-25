package com.houzicore.shared.core.visibility;

import java.util.Iterator;

import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.timing.TimingManager;

import org.bukkit.entity.Player;

public class VisibilityData {
	private final NautHashMap<Player, Boolean> _shouldHide = new NautHashMap<>();
	private final NautHashMap<Player, Boolean> _lastState = new NautHashMap<>();

	// Process New
	private boolean attemptToProcess(Player player, Player target, boolean hide) {
		TimingManager.startTotal("VisData attemptToProcess");

		if (Recharge.Instance.use(player, "VIS " + target.getName(), 250, false, false)) {
			if (hide) {
				TimingManager.startTotal("Hide Player");
				player.hidePlayer(VisibilityManager.Instance.getPlugin(), target);
				TimingManager.stopTotal("Hide Player");
			} else {
				TimingManager.startTotal("Show Player");
				player.showPlayer(VisibilityManager.Instance.getPlugin(), target);
				TimingManager.stopTotal("Show Player");
			}

			_lastState.put(target, hide);

			TimingManager.stopTotal("VisData attemptToProcess");
			return true;
		}

		TimingManager.stopTotal("VisData attemptToProcess");
		return false;
	}

	// Process Update
	public void attemptToProcessUpdate(Player player) {
		TimingManager.startTotal("VisData attemptToProcessUpdate shouldHide");
		if (!_shouldHide.isEmpty()) {
			for (final Iterator<Player> targetIter = _shouldHide.keySet().iterator(); targetIter.hasNext();) {
				final Player target = targetIter.next();
				final boolean hide = _shouldHide.get(target);

				if (!target.isOnline() || !target.isValid() || attemptToProcess(player, target, hide)) {
					targetIter.remove();
				}
			}
		}
		TimingManager.stopTotal("VisData attemptToProcessUpdate shouldHide");

		TimingManager.startTotal("VisData attemptToProcessUpdate lastState");
		if (!_lastState.isEmpty()) {
			for (final Iterator<Player> targetIter = _lastState.keySet().iterator(); targetIter.hasNext();) {
				final Player target = targetIter.next();

				if (!target.isOnline() || !target.isValid()) {
					targetIter.remove();
				}
			}
		}
		TimingManager.stopTotal("VisData attemptToProcessUpdate lastState");
	}

	public void updatePlayer(Player player, Player target, boolean hide) {
		TimingManager.startTotal("VisData updatePlayer");

		if (_lastState.containsKey(target) && _lastState.get(target) == hide) {
			// Already this state, do nothing
			TimingManager.stopTotal("VisData updatePlayer");
			return;
		}

		if (attemptToProcess(player, target, hide)) {
			// Clear old
			_shouldHide.remove(target);
		} else {
			// Store
			_shouldHide.put(target, hide);
		}

		TimingManager.stopTotal("VisData updatePlayer");
	}
}
