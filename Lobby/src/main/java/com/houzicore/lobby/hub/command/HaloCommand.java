package com.houzicore.lobby.hub.command;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.joml.Vector3f;

import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.core.displayentity.DisplayEntityManager;
import com.houzicore.shared.core.displayentity.DisplayModel;
import com.houzicore.shared.core.displayentity.DisplayPart;
import com.houzicore.shared.core.displayentity.ModelAnimation;
import com.houzicore.lobby.hub.HubManager;

public class HaloCommand extends CommandBase<HubManager> {

    private final DisplayEntityManager _displayManager;

    public HaloCommand(HubManager plugin, DisplayEntityManager displayManager) {
        super(plugin, Rank.ADMIN, "halo");
        _displayManager = displayManager;
    }

    @Override
    public void Execute(Player caller, String[] args) {
        // First check if the player already has a halo
        String haloId = "halo_" + caller.getName();
        DisplayModel existing = _displayManager.getModel(haloId);
        
        if (existing != null) {
            _displayManager.removeModel(haloId);
            UtilPlayer.message(caller, F.main("Display", "Removed your cosmetic Halo."));
            return;
        }

        // Create the Halo (a floating gold block or item)
        // By using an ITEM_DISPLAY or BLOCK_DISPLAY, we can make it look like a crown or pet
        DisplayPart haloPart = DisplayPart.block(Material.GOLD_BLOCK);
        
        // Scale it down to look like a small pet/halo
        haloPart.scale(new Vector3f(0.5f, 0.5f, 0.5f));
        // Center the 0.5 block
        haloPart.translation(new Vector3f(-0.25f, -0.25f, -0.25f));

        DisplayModel halo = new DisplayModel(haloId, haloPart);
        
        // Apply bobbing animation
        halo.setAnimation(ModelAnimation.bob(0.4f));
        
        // Follow the player directly above their head
        halo.setFollowEntity(caller, 0, 2.2, 0);

        // Spawn and register
        halo.spawn(caller.getLocation().add(0, 2.2, 0));
        _displayManager.addModel(halo);

        UtilPlayer.message(caller, F.main("Display", "Equipped a cosmetic Halo! Type /halo again to remove."));
    }
}
