package com.houzicore.shared.recharge.command;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.recharge.Recharge;

public class CooldownCommand extends CommandBase<Recharge> {

    public CooldownCommand(Recharge plugin) {
        super(plugin, Rank.ADMIN, "cooldown", "cd");
    }

    @Override
    public void Execute(Player caller, String[] args) {
        if (Plugin.isIgnoreCooldowns(caller)) {
            Plugin.setIgnoreCooldowns(caller, false);
            UtilPlayer.message(caller, F.main("Recharge", "Cooldowns are now " + F.elem("Enabled") + "."));
        } else {
            Plugin.setIgnoreCooldowns(caller, true);
            UtilPlayer.message(caller, F.main("Recharge", "Cooldowns are now " + F.elem("Bypassed") + "."));
        }
    }
}
