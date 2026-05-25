package com.houzicore.shared.core.displayentity.command;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.core.displayentity.DisplayEntityManager;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;

public class GiveFurnitureCommand extends CommandBase<MiniPlugin> {

    private final DisplayEntityManager _manager;

    public GiveFurnitureCommand(MiniPlugin plugin, DisplayEntityManager manager) {
        super(plugin, Rank.ADMIN, "givefurniture", "gf", "bdefurniture");
        _manager = manager;
    }

    /** Convenience constructor — allows addCommand(new GiveFurnitureCommand(displayEntityManager)) */
    public GiveFurnitureCommand(DisplayEntityManager manager) {
        super(null, Rank.ADMIN, "givefurniture", "gf", "bdefurniture");
        _manager = manager;
    }

    @Override
    public void Execute(Player caller, String[] args) {
        if (args.length == 0) {
            UtilPlayer.message(caller, F.main("Furniture", "Usage: /givefurniture <modelId>"));
            return;
        }

        String modelId = args[0].toLowerCase();
        
        ItemStack item = _manager.getFurnitureManager().createFurnitureItem(modelId);
        if (item == null) {
            UtilPlayer.message(caller, F.main("Furniture", "Model not found: " + modelId));
            return;
        }

        caller.getInventory().addItem(item);
        UtilPlayer.message(caller, F.main("Furniture", "Given furniture item: " + modelId));
    }
}
