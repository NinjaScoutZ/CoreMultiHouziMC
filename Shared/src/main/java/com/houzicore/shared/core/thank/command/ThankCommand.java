package com.houzicore.shared.core.thank.command;

import org.bukkit.entity.Player;

import com.houzicore.shared.common.Rank;
import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.core.thank.ThankManager;

public class ThankCommand extends CommandBase<ThankManager> {
    public ThankCommand(ThankManager plugin) {
        super(plugin, Rank.ALL, "thank");
    }

    @Override
    public void Execute(Player caller, String[] args) {
        if (args.length == 0) {
            caller.sendMessage(F.main(Plugin.getName(), "Usage: /thank <player>"));
            return;
        }
        
        Plugin.attemptThank(caller, args[0]);
    }
}
