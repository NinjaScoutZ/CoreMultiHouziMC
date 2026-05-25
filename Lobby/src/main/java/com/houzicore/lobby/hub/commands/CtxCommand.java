package com.houzicore.lobby.hub.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.houzicore.shared.common.actionbar.ActionBarDebugSupport;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.lobby.hub.HubManager;

public class CtxCommand extends CommandBase<HubManager> {

    public CtxCommand(HubManager plugin) {
        super(plugin, Rank.DEVELOPER, "ctx");
    }

    @Override
    public void Execute(Player caller, String[] args) {
        if (args != null && args.length > 0 && args[0].equalsIgnoreCase("reconcile")) {
            Plugin.getTransitionCoordinator().reconcile(caller, args);
        } else if (args != null && args.length > 0 && args[0].equalsIgnoreCase("actionbar")) {
            ActionBarDebugSupport.execute(caller, Arrays.copyOfRange(args, 1, args.length), "/ctx actionbar", Rank.DEVELOPER);
        } else {
            caller.sendMessage("§cUsage: /ctx reconcile <player>");
            caller.sendMessage("§cUsage: /ctx actionbar [show|clear|send]");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String commandLabel, String[] args) {
        if (args.length == 1) {
            return getMatches(args[0], Arrays.asList("reconcile", "actionbar"));
        }

        if (args.length > 1 && args[0].equalsIgnoreCase("actionbar")) {
            return ActionBarDebugSupport.tabComplete(sender, Arrays.copyOfRange(args, 1, args.length));
        }

        return super.onTabComplete(sender, commandLabel, args);
    }
}
