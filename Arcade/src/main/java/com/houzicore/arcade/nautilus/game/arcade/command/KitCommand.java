package com.houzicore.arcade.nautilus.game.arcade.command;

import org.bukkit.entity.Player;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;

public class KitCommand extends CommandBase<ArcadeManager> {

    public KitCommand(ArcadeManager plugin) {
        super(plugin, Rank.ADMIN, "kit", "k");
    }

    @Override
    public void Execute(Player caller, String[] args) {
        Game game = Plugin.GetGame();
        if (game == null) {
            UtilPlayer.message(caller, F.main("Kit", "There is no active game."));
            return;
        }

        if (args.length == 0) {
            Plugin.GetShop().openPageForPlayer(caller, new com.houzicore.arcade.nautilus.game.arcade.kit.ui.KitPage(Plugin, Plugin.GetShop(), Plugin.GetClients(), Plugin.GetDonation(), caller, game));
            return;
        }

        String search = args[0].toLowerCase();
        Kit match = null;

        for (Kit kit : game.GetKits()) {
            if (kit.GetName().toLowerCase().contains(search)) {
                match = kit;
                break;
            }
        }

        if (match == null) {
            UtilPlayer.message(caller, F.main("Kit", "Could not find kit matching " + F.elem(args[0]) + "."));
            return;
        }

        caller.getInventory().clear();
        caller.getInventory().setArmorContents(null);

        game.SetKit(caller, match, false);
        match.ApplyKit(caller);

        UtilPlayer.message(caller, F.main("Kit", "You have equipped " + F.elem(match.GetFormattedName()) + "."));
    }
}
