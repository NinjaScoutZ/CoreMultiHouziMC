package com.houzicore.shared.core.treasure;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.inventory.InventoryManager;

/**
 * Thin facade over InventoryManager — single owner for add / consume / count
 * of treasure chest items. All other classes must go through this service
 * instead of calling InventoryManager directly for chest items.
 */
public class TreasureInventoryService {

    private final InventoryManager _inventoryManager;

    public TreasureInventoryService(InventoryManager inventoryManager) {
        _inventoryManager = inventoryManager;
    }

    /** How many chests of the given type does the player own? */
    public int getOwnedCount(Player player, TreasureType type) {
        return _inventoryManager.Get(player).getItemCount(type.getItemName());
    }

    /**
     * Add qty chests to the player's inventory.
     * qty must be positive. A single DB call is made per chest item name
     * (InventoryManager batches writes in its queue anyway).
     */
    public void addChests(Player player, TreasureType type, int qty) {
        if (qty <= 0) return;
        _inventoryManager.addItemToInventory(player, "Item", type.getItemName(), qty);
    }

    /**
     * Attempt to consume one chest from the player's inventory.
     * Returns true if successful (the player had at least one).
     */
    public boolean consumeChest(Player player, TreasureType type) {
        if (getOwnedCount(player, type) <= 0) return false;
        _inventoryManager.addItemToInventory(player, "Item", type.getItemName(), -1);
        return true;
    }
}
