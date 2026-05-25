package com.houzicore.shared.core.booster;

import org.bukkit.entity.Player;
import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;

public class BoosterCommand extends CommandBase<BoosterManager> {

    public BoosterCommand(BoosterManager plugin) {
        super(plugin, Rank.ALL, "booster");
    }

    @Override
    public void Execute(Player caller, String[] args) {
        if (args == null || args.length == 0) {
            UtilPlayer.message(caller, F.main("Booster", "Usage: /booster activate"));
            return;
        }

        if (args[0].equalsIgnoreCase("activate")) {
            Plugin.attemptActivateBooster(caller);
        } else {
            UtilPlayer.message(caller, F.main("Booster", "Usage: /booster activate"));
        }
    }
}
