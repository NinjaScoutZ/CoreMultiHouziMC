package com.houzicore.shared.core.friend.command;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.core.friend.FriendManager;

public class DeleteFriend extends CommandBase<FriendManager> {
	public DeleteFriend(FriendManager plugin) {
		super(plugin, Rank.ALL, "unfriend");
	}

	@Override
	public void Execute(final Player caller, final String[] args) {
		if (args == null) {
			F.main(Plugin.getName(), "You need to include a player's name.");
		} else {
			CommandCenter.GetClientManager().checkPlayerName(caller, args[0], new Callback<String>() {
				@Override
				public void run(String result) {
					if (result != null) {
						Plugin.removeFriend(caller, result);
					}
				}
			});
		}
	}
}
