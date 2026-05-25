package com.houzicore.shared.core.gadget.types;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

/**
 * Base class for Aura gadgets.
 * Passive particle effect that continuously plays around the player.
 */
public abstract class AuraGadget extends Gadget {

    private final CosmeticRarity _rarity;

    public AuraGadget(GadgetManager manager, String name, String[] desc,
            int cost, Material displayMaterial, byte displayData, CosmeticRarity rarity) {
        super(manager, GadgetType.Aura, name, desc, cost, displayMaterial, displayData);
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

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (event.getType() != UpdateType.TICK) return;

        for (Player player : GetActive()) {
            if (player.isOnline()) {
                renderAura(player, player.getTicksLived());
            }
        }
    }

    protected abstract void renderAura(Player player, int tick);
}
