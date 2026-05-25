package com.houzicore.shared.core.friend.command;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.core.friend.FriendManager;
import com.houzicore.shared.core.friend.ui.FriendShop;
import com.houzicore.shared.core.preferences.UserPreferences;

public class FriendsDisplay extends CommandBase<FriendManager> {
	public FriendsDisplay(FriendManager plugin) {
		super(plugin, Rank.ALL, "friendsdisplay");
	}

	@Override
	public void Execute(Player caller, final String[] args) {
		final UserPreferences preferences = Plugin.getPreferenceManager().Get(caller);

		preferences.friendDisplayInventoryUI = !preferences.friendDisplayInventoryUI;

		Plugin.getPreferenceManager().savePreferences(caller);

		caller.playSound(caller.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1.6f);

		if (preferences.friendDisplayInventoryUI) {
			Plugin.getShop().attemptShopOpen(caller);
		} else {
			Plugin.showFriends(caller);
		}
	}
}
