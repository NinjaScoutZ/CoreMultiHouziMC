package com.houzicore.shared.core.friend.command;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.core.friend.FriendManager;
import com.houzicore.shared.core.friend.data.FriendStatus;

public class FavoriteFriend extends CommandBase<FriendManager> {
	public FavoriteFriend(FriendManager plugin) {
		super(plugin, Rank.ALL, "friendfavorite", "ff");
	}

	@Override
	public void Execute(final Player caller, final String[] args) {
		if (args == null || args.length < 1) {
			caller.sendMessage(org.bukkit.ChatColor.RED + "Usage: /ff <player>");
		} else {
			CommandCenter.GetClientManager().checkPlayerName(caller, args[0], new Callback<String>() {
				@Override
				public void run(String result) {
					if (result != null) {
						boolean currentFavorite = false;
						for (FriendStatus status : Plugin.Get(caller).getFriends()) {
							if (status.Name.equalsIgnoreCase(result)) {
								currentFavorite = status.Favorite;
								break;
							}
						}
						Plugin.updateFavorite(caller, result, !currentFavorite);
						caller.sendMessage(org.bukkit.ChatColor.YELLOW + result + org.bukkit.ChatColor.GRAY + " has been " + (!currentFavorite ? "added to" : "removed from") + " your favorites.");
					}
				}
			});
		}
	}
}
