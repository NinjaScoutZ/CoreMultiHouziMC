package com.houzicore.shared.common.util;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GuiUtil {

    private static final Material DEFAULT_BORDER = Material.BLACK_STAINED_GLASS_PANE;

    public static void fillBorders(Inventory inventory) {
        fillBorders(inventory, DEFAULT_BORDER);
    }

    public static void fillBorders(Inventory inventory, Material material) {
        int rows = inventory.getSize() / 9;
        ItemStack borderPane = createPane(material, " ");

        for (int row = 0; row < rows; row++) {
            if (row == 0 || row == rows - 1) {
                for (int column = 0; column < 9; column++) {
                    inventory.setItem(column + (row * 9), borderPane);
                }
            } else {
                inventory.setItem(row * 9, borderPane);
                inventory.setItem(row * 9 + 8, borderPane);
            }
        }
    }

    /**
     * Fill a single row's empty slots with colored glass panes (HouziCore UI pattern).
     */
    public static void fillRow(Inventory inventory, int rowStart, Material glassMaterial, String title) {
        ItemStack pane = createPane(glassMaterial, title);
        for (int i = rowStart; i < rowStart + 9 && i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, pane);
            }
        }
    }

    /**
     * Fill all empty slots with currency-labeled glass panes (HouziCore UI signature).
     */
    public static void fillAllEmpty(Inventory inventory, Material glassMaterial, String label) {
        ItemStack pane = createPane(glassMaterial, label);
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, pane);
            }
        }
    }

    private static ItemStack createPane(Material material, String title) {
        ItemStack pane = new ItemStack(material);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(title);
            pane.setItemMeta(meta);
        }
        return pane;
    }
}
