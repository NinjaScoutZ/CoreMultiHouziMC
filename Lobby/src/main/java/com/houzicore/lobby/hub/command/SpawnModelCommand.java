package com.houzicore.lobby.hub.command;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.core.displayentity.DisplayEntityManager;
import com.houzicore.shared.core.displayentity.DisplayModel;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;

public class SpawnModelCommand extends CommandBase<com.houzicore.lobby.hub.HubManager> {

    private final DisplayEntityManager _displayManager;

    public SpawnModelCommand(com.houzicore.lobby.hub.HubManager plugin, DisplayEntityManager displayManager) {
        super(plugin, Rank.ADMIN, "spawnmodel");
        _displayManager = displayManager;
    }

    @Override
    public void Execute(Player caller, String[] args) {
        if (args.length == 0) {
            UtilPlayer.message(caller, F.main("Display", "Usage: /spawnmodel <id> [with-seat]"));
            return;
        }

        String modelId = args[0].toLowerCase();
        DisplayModel template = _displayManager.getRegistry().getModel(modelId);

        if (template == null) {
            UtilPlayer.message(caller, F.main("Display", "Model not found: " + modelId));
            return;
        }

        // Clone the model so we don't modify the global template
        DisplayModel model = template.copy(modelId + "_" + System.currentTimeMillis());

        // Check if admin requested testing the hitboxes
        if (args.length > 1 && args[1].equalsIgnoreCase("with-seat")) {
            // Adds a 1x1x1 solid block collision in the center
            model.addSolidHitbox(0, 0, 0, 1.0);
            
            // Adds an interactable seat zone (W: 1.5, H: 1.0) slightly above ground
            model.addSeat(0, 0.4, 0, 1.5f, 1.0f);
            
            UtilPlayer.message(caller, F.main("Display", "Injected physical Hitbox and Seat."));
        }

        model.spawn(caller.getLocation().add(0, 0, 0));
        _displayManager.addModel(model);
        UtilPlayer.message(caller, F.main("Display", "Spawned model: " + modelId));
    }
}
