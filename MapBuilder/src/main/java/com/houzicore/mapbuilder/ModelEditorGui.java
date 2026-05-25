package com.houzicore.mapbuilder;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ModelEditorGui implements Listener {

    private static final String TITLE_PREFIX = ChatColor.LIGHT_PURPLE + "Edit Model: ";

    public ModelEditorGui() {
        // Register globally via MapBuilderPlugin
        Bukkit.getPluginManager().registerEvents(this, MapBuilderPlugin.getInstance());
    }

    public static void openGui(Player player, Location loc) {
        String title = TITLE_PREFIX + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
        Inventory inv = Bukkit.createInventory(null, 54, title);

        // Fetch current values to display
        float[] t = getCurrentTransforms(player, loc);
        
        // Navigation Options
        // X Nudge
        inv.setItem(10, createBtn(Material.CRIMSON_BUTTON, ChatColor.RED + "X-Offset (-0.1)", t[5] + ""));
        inv.setItem(19, createBtn(Material.NETHERRACK, ChatColor.RED + "X-Offset (-1.0)", t[5] + ""));
        inv.setItem(12, createBtn(Material.WARPED_BUTTON, ChatColor.AQUA + "X-Offset (+0.1)", t[5] + ""));
        inv.setItem(21, createBtn(Material.WARPED_NYLIUM, ChatColor.AQUA + "X-Offset (+1.0)", t[5] + ""));

        // Y Nudge
        inv.setItem(13, createBtn(Material.CRIMSON_BUTTON, ChatColor.RED + "Y-Offset (-0.1)", t[6] + ""));
        inv.setItem(22, createBtn(Material.NETHERRACK, ChatColor.RED + "Y-Offset (-1.0)", t[6] + ""));
        inv.setItem(15, createBtn(Material.WARPED_BUTTON, ChatColor.AQUA + "Y-Offset (+0.1)", t[6] + ""));
        inv.setItem(24, createBtn(Material.WARPED_NYLIUM, ChatColor.AQUA + "Y-Offset (+1.0)", t[6] + ""));

        // Z Nudge
        inv.setItem(16, createBtn(Material.CRIMSON_BUTTON, ChatColor.RED + "Z-Offset (-0.1)", t[7] + ""));
        inv.setItem(25, createBtn(Material.NETHERRACK, ChatColor.RED + "Z-Offset (-1.0)", t[7] + ""));
        inv.setItem(17, createBtn(Material.WARPED_BUTTON, ChatColor.AQUA + "Z-Offset (+0.1)", t[7] + ""));
        inv.setItem(26, createBtn(Material.WARPED_NYLIUM, ChatColor.AQUA + "Z-Offset (+1.0)", t[7] + ""));

        // Rotation
        inv.setItem(37, createBtn(Material.ACACIA_BUTTON, ChatColor.YELLOW + "Yaw Left (-5°)", t[0] + ""));
        inv.setItem(38, createBtn(Material.ACACIA_FENCE, ChatColor.YELLOW + "Yaw Right (+5°)", t[0] + ""));
        inv.setItem(40, createBtn(Material.BIRCH_BUTTON, ChatColor.GOLD + "Pitch Down (-5°)", t[1] + ""));
        inv.setItem(41, createBtn(Material.BIRCH_FENCE, ChatColor.GOLD + "Pitch Up (+5°)", t[1] + ""));

        // Scale Global
        inv.setItem(43, createBtn(Material.CHISELED_STONE_BRICKS, ChatColor.GREEN + "Scale Down (-0.1)", "Scale: " + t[2] + "x"));
        inv.setItem(44, createBtn(Material.EMERALD_BLOCK, ChatColor.GREEN + "Scale Up (+0.1)", "Scale: " + t[2] + "x"));

        player.openInventory(inv);
    }

    private static ItemStack createBtn(Material mat, String name, String currentVal) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Current: " + ChatColor.WHITE + currentVal));
        item.setItemMeta(meta);
        return item;
    }

    private static float[] getCurrentTransforms(Player player, Location loc) {
        MapSession session = MapBuilderPlugin.getInstance().getSession(player);
        if (session == null) return new float[]{0,0,1,1,1,0,0,0};

        for (String type : session.getDataPoints().keySet()) {
            if (type.startsWith("DISPLAY_MODEL:") && session.getDataPoints().get(type).contains(loc)) {
                String[] parts = type.substring("DISPLAY_MODEL:".length()).split(":");
                if (parts.length > 1) {
                    try {
                        String[] tf = parts[1].split(",");
                        return new float[]{
                            Float.parseFloat(tf[0]), Float.parseFloat(tf[1]),
                            Float.parseFloat(tf[2]), Float.parseFloat(tf[3]), Float.parseFloat(tf[4]),
                            Float.parseFloat(tf[5]), Float.parseFloat(tf[6]), Float.parseFloat(tf[7])
                        };
                    } catch (Exception e) {}
                }
                return new float[]{0,0,1,1,1,0,0,0};
            }
        }
        return new float[]{0,0,1,1,1,0,0,0};
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getView().getTitle() == null || !event.getView().getTitle().startsWith(TITLE_PREFIX)) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        MapSession session = MapBuilderPlugin.getInstance().getSession(player);
        if (session == null) {
            player.closeInventory();
            return;
        }

        ItemStack item = event.getCurrentItem();
        if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;
        String name = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        // Parse location from title
        String coords = event.getView().getTitle().replace(TITLE_PREFIX, "");
        String[] cparts = coords.split(",");
        Location loc = new Location(player.getWorld(), Integer.parseInt(cparts[0]), Integer.parseInt(cparts[1]), Integer.parseInt(cparts[2]));

        // Find the current type string
        String currentType = null;
        for (String t : session.getDataPoints().keySet()) {
            if (t.startsWith("DISPLAY_MODEL:") && session.getDataPoints().get(t).contains(loc)) {
                currentType = t;
                break;
            }
        }
        if (currentType == null) return;

        // Perform edit
        float[] t = getCurrentTransforms(player, loc);
        
        if (name.contains("X-Offset (-0.1)")) t[5] -= 0.1f;
        else if (name.contains("X-Offset (-1.0)")) t[5] -= 1.0f;
        else if (name.contains("X-Offset (+0.1)")) t[5] += 0.1f;
        else if (name.contains("X-Offset (+1.0)")) t[5] += 1.0f;
        
        else if (name.contains("Y-Offset (-0.1)")) t[6] -= 0.1f;
        else if (name.contains("Y-Offset (-1.0)")) t[6] -= 1.0f;
        else if (name.contains("Y-Offset (+0.1)")) t[6] += 0.1f;
        else if (name.contains("Y-Offset (+1.0)")) t[6] += 1.0f;
        
        else if (name.contains("Z-Offset (-0.1)")) t[7] -= 0.1f;
        else if (name.contains("Z-Offset (-1.0)")) t[7] -= 1.0f;
        else if (name.contains("Z-Offset (+0.1)")) t[7] += 0.1f;
        else if (name.contains("Z-Offset (+1.0)")) t[7] += 1.0f;

        else if (name.contains("Yaw Left")) t[0] -= 5.0f;
        else if (name.contains("Yaw Right")) t[0] += 5.0f;
        else if (name.contains("Pitch Down")) t[1] -= 5.0f;
        else if (name.contains("Pitch Up")) t[1] += 5.0f;

        else if (name.contains("Scale Down")) { t[2] -= 0.1f; t[3] -= 0.1f; t[4] -= 0.1f; }
        else if (name.contains("Scale Up")) { t[2] += 0.1f; t[3] += 0.1f; t[4] += 0.1f; }

        // Compile new type string
        String modelId = currentType.substring("DISPLAY_MODEL:".length()).split(":")[0];
        String newStr = String.format("%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f", t[0], t[1], t[2], t[3], t[4], t[5], t[6], t[7]);
        String newType = "DISPLAY_MODEL:" + modelId + ":" + newStr;

        // Apply!
        session.getDataPoints().get(currentType).remove(loc);
        VisualManager.getInstance().removeVisual(loc);
        
        session.addDataPoint(newType, loc);
        VisualManager.getInstance().spawnVisual(player.getUniqueId(), newType, loc);

        // Re-open with new stats
        openGui(player, loc);
    }
}
