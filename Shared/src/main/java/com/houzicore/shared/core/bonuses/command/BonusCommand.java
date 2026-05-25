package com.houzicore.shared.core.bonuses.command;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.core.bonuses.BonusManager;
import com.houzicore.shared.core.bonuses.BonusMenu;

public class BonusCommand extends CommandBase<BonusManager> {

    public BonusCommand(BonusManager plugin) {
        super(plugin, Rank.ALL, "bonus", "keeper");
    }

    @Override
    public void Execute(Player caller, String[] args) {
        BonusMenu menu = new BonusMenu(Plugin, Plugin.getClientManager(), Plugin.getDonationManager());
        menu.attemptShopOpen(caller);
    }
}
