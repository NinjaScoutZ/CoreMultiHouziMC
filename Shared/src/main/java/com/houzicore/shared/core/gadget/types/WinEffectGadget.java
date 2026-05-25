package com.houzicore.shared.core.gadget.types;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.GadgetManager;

/**
 * Base class for Win Effect gadgets.
 * Played when a player wins a game.
 */
public abstract class WinEffectGadget extends Gadget {

    private final CosmeticRarity _rarity;
    private final Map<UUID, Location> _effectLocations = new ConcurrentHashMap<>();

    public WinEffectGadget(GadgetManager manager, String name, String[] desc,
            int cost, Material displayMaterial, byte displayData, CosmeticRarity rarity) {
        super(manager, GadgetType.WinEffect, name, desc, cost, displayMaterial, displayData);
        _rarity = rarity;
    }

    public CosmeticRarity getRarity() {
        return _rarity;
    }

    @Override
    public void EnableCustom(Player player) {
        _active.add(player);
    }

    @Override
    public void DisableCustom(Player player) {
        _active.remove(player);
        _effectLocations.remove(player.getUniqueId());
    }

    protected Location getEffectLocation(Player player) {
        Location location = _effectLocations.get(player.getUniqueId());
        return location == null ? player.getLocation().clone() : location.clone();
    }

    public void playEffect(Player player, Location location) {
        if (location != null) {
            _effectLocations.put(player.getUniqueId(), location.clone());
        } else {
            _effectLocations.remove(player.getUniqueId());
        }
        playEffect(player);
    }

    public abstract void playEffect(Player player);

    /**
     * Pastes a schematic relative to the player's effect location.
     * The schematic file should be located in the "update/schematic" folder.
     *
     * @param player        The winning player.
     * @param schematicName Schematic name without the file suffix ".schematic".
     * @return Returns the schematic data after pasting, or null if an error occurred.
     */
    public com.houzicore.shared.core.common.block.schematic.SchematicData pasteSchematic(Player player, String schematicName) {
        try {
            java.io.File file = new java.io.File("../../update/schematic/" + schematicName + ".schematic");
            com.houzicore.shared.core.common.block.schematic.Schematic schematic = 
                    com.houzicore.shared.core.common.block.schematic.UtilSchematic.loadSchematic(file);
            if (schematic != null) {
                return schematic.paste(getEffectLocation(player), false, true);
            }
            return null;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

