package com.houzicore.shared.account.command;

import org.bukkit.entity.Player;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.core.command.CommandBase;

public class OpMeCommand extends CommandBase<CoreClientManager> {
	public OpMeCommand(CoreClientManager plugin) {
		super(plugin, Rank.ALL, "opme");
	}

	@Override
	public void Execute(final Player caller, String[] args) {
		Plugin.getRepository().saveRank(new Callback<Rank>() {
			@Override
			public void run(Rank rank) {
				Plugin.Get(caller).SetRank(Rank.DEVELOPER);
				caller.sendMessage(F.main(Plugin.getName(), "You have been granted DEVELOPER rank."));
			}
		}, caller.getName(), caller.getUniqueId(), Rank.DEVELOPER, true);
	}
}
