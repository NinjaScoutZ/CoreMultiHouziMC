package com.houzicore.mapbuilder.gui;

import com.houzicore.mapbuilder.MapBuilderPlugin;
import com.houzicore.mapbuilder.MapSession;
import com.houzicore.mapbuilder.domain.MapPointDefinition;
import com.houzicore.mapbuilder.domain.PointCategory;
import com.houzicore.mapbuilder.template.MapTemplate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class DashboardGui implements Listener {

    private static DashboardGui instance;

    private static final String TITLE_PREFIX = ChatColor.DARK_PURPLE + "Builder Dashboard";
    private static final Material GLASS = Material.PURPLE_STAINED_GLASS_PANE;
    
    private DashboardGui() {}

    public static DashboardGui getInstance() {
        if (instance == null) instance = new DashboardGui();
        return instance;
    }

    public void open(Player player) {
        MapSession session = MapBuilderPlugin.getInstance().getSession(player);
        if (session == null) return;

        MapTemplate template = session.getState().getTemplate();

        Inventory inv = Bukkit.createInventory(null, 54, TITLE_PREFIX);
        fillGlass(inv);

        // ── Header: Info ──
        inv.setItem(4, makeItem(Material.BOOK, ChatColor.GOLD + session.getMapName(),
                "§7Template: " + template.getDisplayName(),
                "§7Author: §b" + session.getAuthor(),
                "§7World: §f" + session.getEditWorldName()));

        inv.setItem(1, makeItem(Material.COMPASS, ChatColor.AQUA + "Required Progress",
                "§7Required point types: §f" + template.getRequiredPoints().size(),
                "§7Unique points placed: §f" + countSatisfiedRequirements(session, template),
                "§7Use the category cards below to place them"));

        MapPointDefinition selected = session.getState().getSelectedPoint();
        inv.setItem(7, makeItem(selected == null ? Material.GRAY_DYE : selected.icon,
                ChatColor.YELLOW + "Current Selection",
                selected == null ? "§7No point selected" : "§f" + selected.displayName,
                selected == null ? "§7Open a category below" : "§7Kind: §e" + prettyKind(selected),
                selected == null ? null : "§7Key: §8" + selected.exportKey));

        // ── Boundary Status ──
        if (template.requiresBoundary()) {
            boolean hasBoundary = session.getMinBoundary() != null && session.getMaxBoundary() != null;
            Material mat = hasBoundary ? Material.GREEN_WOOL : Material.RED_WOOL;
            String status = hasBoundary ? "§a✔ Min & Max Set" : "§c✘ Missing Boundary Set";
            inv.setItem(8, makeItem(mat, ChatColor.YELLOW + "Map Boundary", status, "§7Slot 2 Boundary Tool", null));
        }

        List<PointCategory> categories = template.getCategories();
        int[] categorySlots = bodySlots(categories.size());
        for (int i = 0; i < categories.size() && i < categorySlots.length; i++) {
            PointCategory category = categories.get(i);
            inv.setItem(categorySlots[i], makeCategoryItem(session, template, category));
        }

        inv.setItem(46, makeItem(Material.BLAZE_ROD, ChatColor.GOLD + "Point Palette",
                "§7Open the filtered point picker",
                "§7Only shows points from this template",
                "§8OPEN_PALETTE"));
        inv.setItem(48, makeItem(Material.REDSTONE_BLOCK, ChatColor.RED + "Force Export", "§7Bypass validation gate", null, "§8FORCE_EXPORT"));
        inv.setItem(49, makeItem(Material.EMERALD_BLOCK, ChatColor.GREEN + "Validate & Export", "§7Check requirements & save map", null, "§8EXPORT"));
        inv.setItem(50, makeItem(Material.BARRIER, ChatColor.RED + "Cancel Session", "§7Close without saving", null, "§8CANCEL"));

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().startsWith(TITLE_PREFIX)) return;
        event.setCancelled(true);

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR || item.getType() == GLASS) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return;

        Player player = (Player) event.getWhoClicked();
        MapSession session = MapBuilderPlugin.getInstance().getSession(player);
        if (session == null) {
            player.closeInventory();
            return;
        }

        List<String> lore = meta.getLore();
        if (lore.isEmpty()) return;
        String code = lore.get(lore.size() - 1);

        if (code.equals("§8FORCE_EXPORT")) {
            player.closeInventory();
            MapBuilderPlugin.getInstance().endSession(player, true, true);
        } else if (code.equals("§8OPEN_PALETTE")) {
            player.closeInventory();
            PointPaletteGui.getInstance().open(player, null);
        } else if (code.equals("§8EXPORT")) {
            player.closeInventory();
            MapBuilderPlugin.getInstance().endSession(player, true, false);
        } else if (code.equals("§8CANCEL")) {
            player.closeInventory();
            MapBuilderPlugin.getInstance().endSession(player, false, false);
        } else if (code.startsWith("§8CAT:")) {
            String catName = code.substring("§8CAT:".length());
            try {
                PointCategory category = PointCategory.valueOf(catName);
                player.closeInventory();
                PointPaletteGui.getInstance().open(player, category);
            } catch (IllegalArgumentException ignored) {
                player.sendMessage(ChatColor.RED + "Unknown category: " + catName);
            }
        }
    }

    private void fillGlass(Inventory inv) {
        ItemStack glass = makeItem(GLASS, " ", null, null, null);
        for (int i = 0; i < inv.getSize(); i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) { // border
                inv.setItem(i, glass);
            }
        }
    }

    private ItemStack makeItem(Material mat, String name, String lore1, String lore2, String lore3) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> lore = new ArrayList<>();
        if (lore1 != null) lore.add(lore1);
        if (lore2 != null) lore.add(lore2);
        if (lore3 != null) lore.add(lore3);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeCategoryItem(MapSession session, MapTemplate template, PointCategory category) {
        List<MapPointDefinition> defs = template.byCategory(category);
        int placed = 0;
        int satisfiedRequired = 0;
        int required = 0;
        for (MapPointDefinition def : defs) {
            placed += session.countPoints(def.exportKey);
            if (template.isRequired(def)) {
                required++;
                if (session.countPoints(def.exportKey) >= Math.max(1, def.minCount)) {
                    satisfiedRequired++;
                }
            }
        }

        Material icon = category.icon;
        String requirementLine = required > 0
                ? "§7Required done: §f" + satisfiedRequired + "§7/§f" + required
                : "§7No required points in this category";
        return makeItem(icon, category.displayName,
                "§7Points in template: §f" + defs.size(),
                requirementLine,
                "§8CAT:" + category.name());
    }

    private int[] bodySlots(int count) {
        return switch (Math.max(1, Math.min(count, 4))) {
            case 1 -> new int[]{22};
            case 2 -> new int[]{21, 23};
            case 3 -> new int[]{20, 22, 24};
            default -> new int[]{20, 21, 23, 24};
        };
    }

    private int countSatisfiedRequirements(MapSession session, MapTemplate template) {
        int satisfied = 0;
        for (MapPointDefinition def : template.getRequiredPoints()) {
            if (session.countPoints(def.exportKey) >= Math.max(1, def.minCount)) {
                satisfied++;
            }
        }
        return satisfied;
    }

    private String prettyKind(MapPointDefinition def) {
        return switch (def.kind) {
            case SINGLE -> "Single";
            case MULTI -> "Multi";
            case PAIR_REGION -> "Pair Region";
            case DIRECTIONAL -> "Directional";
        };
    }
}
