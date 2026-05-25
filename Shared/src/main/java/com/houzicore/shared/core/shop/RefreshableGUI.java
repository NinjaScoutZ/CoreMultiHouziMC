package com.houzicore.shared.core.shop;

import org.bukkit.entity.Player;

/**
 * Interface for GUIs that auto-refresh their contents on a timer.
 * Implement this on any Shop/GUI class, then the parent framework
 * can detect it and register an UpdateEvent loop.
 *
 * <pre>
 * public class LiveStatsShop extends ShopBase implements RefreshableGUI {
 *     {@literal @}Override
 *     public void refreshItems(Player player) {
 *         // Update item stacks here
 *     }
 *     {@literal @}Override
 *     public int refreshRateTicks() { return 20; } // every second
 * }
 * </pre>
 *
 * Ported from: net.swofty.type.generic.gui.inventory.RefreshingGUI
 */
public interface RefreshableGUI {

    /**
     * Called periodically to update GUI items.
     * Implementors should update ItemStacks in slots that need refreshing.
     * This is called async-safe — do NOT open new inventories from here.
     */
    void refreshItems(Player player);

    /**
     * How often to refresh, in server ticks.
     * Default: 20 ticks = 1 second.
     */
    default int refreshRateTicks() {
        return 20;
    }
}
