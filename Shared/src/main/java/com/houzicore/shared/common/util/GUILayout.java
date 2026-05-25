package com.houzicore.shared.common.util;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Static utilities for common GUI layout operations.
 * Eliminates repetitive for-loops when building chest GUIs.
 *
 * <pre>
 * GUILayout.border(inventory, glassPane, 0, 53);  // 6-row border
 * GUILayout.fill(inventory, filler, 10, 16);       // fill a row
 * </pre>
 *
 * Ported from: HypixelInventoryGUI.fill() / border() methods
 * Adapted to static utility pattern (no inheritance required).
 */
public final class GUILayout {

    private GUILayout() {}

    /**
     * Fill a rectangular region between two corner slots.
     * Slot math: row = slot / 9, col = slot % 9
     *
     * @param inv       Target inventory
     * @param item      ItemStack to fill with
     * @param corner1   Top-left slot (0-53)
     * @param corner2   Bottom-right slot (0-53)
     * @param overwrite If false, skip slots that already have items
     */
    public static void fill(Inventory inv, ItemStack item, int corner1, int corner2, boolean overwrite) {
        int row1 = corner1 / 9, col1 = corner1 % 9;
        int row2 = corner2 / 9, col2 = corner2 % 9;
        int minRow = Math.min(row1, row2), maxRow = Math.max(row1, row2);
        int minCol = Math.min(col1, col2), maxCol = Math.max(col1, col2);

        for (int r = minRow; r <= maxRow; r++) {
            for (int c = minCol; c <= maxCol; c++) {
                int slot = r * 9 + c;
                if (slot < 0 || slot >= inv.getSize()) continue;
                if (!overwrite && inv.getItem(slot) != null) continue;
                inv.setItem(slot, item.clone());
            }
        }
    }

    /** Fill with overwrite=true (default). */
    public static void fill(Inventory inv, ItemStack item, int corner1, int corner2) {
        fill(inv, item, corner1, corner2, true);
    }

    /** Fill entire inventory. */
    public static void fillAll(Inventory inv, ItemStack item) {
        fill(inv, item, 0, inv.getSize() - 1, true);
    }

    /**
     * Draw a border (rectangle outline) between two corner slots.
     *
     * Ported from HypixelInventoryGUI.border() — calculates top/bottom rows
     * and left/right columns to draw only the edges.
     */
    public static void border(Inventory inv, ItemStack item, int corner1, int corner2, boolean overwrite) {
        int row1 = corner1 / 9, col1 = corner1 % 9;
        int row2 = corner2 / 9, col2 = corner2 % 9;
        int minRow = Math.min(row1, row2), maxRow = Math.max(row1, row2);
        int minCol = Math.min(col1, col2), maxCol = Math.max(col1, col2);

        for (int r = minRow; r <= maxRow; r++) {
            for (int c = minCol; c <= maxCol; c++) {
                // Only place on edges (top row, bottom row, left col, right col)
                if (r == minRow || r == maxRow || c == minCol || c == maxCol) {
                    int slot = r * 9 + c;
                    if (slot < 0 || slot >= inv.getSize()) continue;
                    if (!overwrite && inv.getItem(slot) != null) continue;
                    inv.setItem(slot, item.clone());
                }
            }
        }
    }

    /** Border with overwrite=true (default). */
    public static void border(Inventory inv, ItemStack item, int corner1, int corner2) {
        border(inv, item, corner1, corner2, true);
    }

    /** Full inventory border. */
    public static void border(Inventory inv, ItemStack item) {
        border(inv, item, 0, inv.getSize() - 1, true);
    }

    /**
     * Find the first empty slot in the inventory.
     * @return slot index, or -1 if inventory is full
     */
    public static int firstEmpty(Inventory inv) {
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) return i;
        }
        return -1;
    }
}
