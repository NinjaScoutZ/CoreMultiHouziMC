package com.houzicore.shared.core.ignore.command;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.core.ignore.IgnoreManager;

import org.bukkit.entity.Player;

public class Unignore extends CommandBase<IgnoreManager> {
	public Unignore(IgnoreManager plugin) {
		super(plugin, Rank.ALL, "unignore");
	}

	@Override
	public void Execute(final Player caller, final String[] args) {
		if (args == null) {
			caller.sendMessage(F.main(Plugin.getName(), "You need to include a player's name."));
		} else {
			CommandCenter.GetClientManager().checkPlayerName(caller, args[0], new Callback<String>() {
				@Override
				public void run(String result) {
					if (result != null) {
						Plugin.removeIgnore(caller, result);
					}
				}
			});
		}
	}
}
