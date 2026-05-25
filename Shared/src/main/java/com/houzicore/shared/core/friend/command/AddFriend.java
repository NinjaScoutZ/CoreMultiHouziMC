package com.houzicore.shared.core.friend.command;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.core.friend.FriendManager;


public class AddFriend extends CommandBase<FriendManager> {
	public AddFriend(FriendManager plugin) {
		super(plugin, Rank.ALL, "friends", "friend", "f");
	}

	@Override
	public void Execute(final Player caller, final String[] args) {
		if (args == null || args.length < 1) {
			if (Plugin.getPreferenceManager().Get(caller).friendDisplayInventoryUI) {
				Plugin.getShop().attemptShopOpen(caller);
			} else {
				Plugin.showFriends(caller);
			}
		} else {
			CommandCenter.GetClientManager().checkPlayerName(caller, args[0], new Callback<String>() {
				@Override
				public void run(String result) {
					if (result != null) {
						Plugin.addFriend(caller, result);
					}
				}
			});
		}
	}
}
