package com.houzicore.shared.core.shop;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Extension of RefreshableGUI that only sends packet updates
 * for slots whose ItemStack actually changed — eliminates flicker.
 */
public interface DiffAwareGUI extends RefreshableGUI {

    /**
     * Returns the inventory backing this GUI.
     */
    Inventory getBackingInventory();

    /**
     * Compute new items and return them as a sparse map: slot→ItemStack.
     * Only return slots that CHANGED since last refresh.
     */
    java.util.Map<Integer, ItemStack> computeChangedSlots(Player player);

    /**
     * Default implementation: only update changed slots instead of full redraw.
     */
    @Override
    default void refreshItems(Player player) {
        Inventory inv = getBackingInventory();
        if (inv == null) return;
        java.util.Map<Integer, ItemStack> changes = computeChangedSlots(player);
        if (changes != null) {
            changes.forEach((slot, item) -> {
                if (slot >= 0 && slot < inv.getSize()) {
                    inv.setItem(slot, item);
                }
            });
        }
    }
}
