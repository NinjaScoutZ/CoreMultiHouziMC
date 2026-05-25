package com.houzicore.shared.core.gadget.types;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.Location;

import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.GadgetManager;

/**
 * Base class for Kill Effect gadgets.
 * Played when a player kills another player.
 */
public abstract class KillEffectGadget extends Gadget {

    private final CosmeticRarity _rarity;

    public KillEffectGadget(GadgetManager manager, String name, String[] desc,
            int cost, Material displayMaterial, byte displayData, CosmeticRarity rarity) {
        super(manager, GadgetType.KillEffect, name, desc, cost, displayMaterial, displayData);
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
    }

    public abstract void playEffect(Player killer, Location deathLoc);
}
