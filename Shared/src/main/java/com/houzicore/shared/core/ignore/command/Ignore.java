package com.houzicore.shared.core.ignore.command;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.core.ignore.IgnoreManager;

import org.bukkit.entity.Player;

public class Ignore extends CommandBase<IgnoreManager> {
	public Ignore(IgnoreManager plugin) {
		super(plugin, Rank.ALL, "ignore");
	}

	@Override
	public void Execute(final Player caller, final String[] args) {
		if (args == null || args.length == 0) {
			if (Plugin.getPreferenceManager().Get(caller).friendDisplayInventoryUI) {
				Plugin.getShop().attemptShopOpen(caller);
			} else {
				Plugin.showIgnores(caller);
			}
		} else {
			CommandCenter.GetClientManager().checkPlayerName(caller, args[0], new Callback<String>() {
				@Override
				public void run(String result) {
					if (result != null) {
						Plugin.addIgnore(caller, result);
					}
				}
			});
		}
	}
}
