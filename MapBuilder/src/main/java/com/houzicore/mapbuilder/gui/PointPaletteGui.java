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

/**
 * Two-panel palette GUI:
 *   Panel A — 54-slot category chooser (one button per PointCategory)
 *   Panel B — 54-slot point list for the chosen category, filtered by template
 *
 * Clicking a point in Panel B selects it and closes the GUI.
 * The "Back" button in Panel B returns to Panel A.
 * The "Deselect" button clears the current selection.
 */
public class PointPaletteGui implements Listener {

    private static PointPaletteGui instance;

    private static final String TITLE_CATEGORY = ChatColor.DARK_PURPLE + "Select Category";
    private static final String TITLE_PREFIX   = ChatColor.GOLD + "Points: ";
    private static final Material GLASS        = Material.PURPLE_STAINED_GLASS_PANE;
    private static final Material BACK_MAT     = Material.ARROW;
    private static final Material DESEL_MAT    = Material.BARRIER;

    private PointPaletteGui() {}

    public static PointPaletteGui getInstance() {
        if (instance == null) instance = new PointPaletteGui();
        return instance;
    }

    // ── Open category chooser ────────────────────────────────────────────────

    public void open(Player player, PointCategory filter) {
        if (filter == null) {
            openCategoryMenu(player);
        } else {
            openCategoryPage(player, filter);
        }
    }

    private void openCategoryMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE_CATEGORY);
        fillGlass(inv);

        MapSession session = MapBuilderPlugin.getInstance().getSession(player);
        MapTemplate template = session != null ? session.getState().getTemplate() : null;

        List<PointCategory> categories = template != null
                ? template.getCategories()
                : List.of(PointCategory.values());
        int[] slots = centeredSlots(categories.size());
        for (int i = 0; i < categories.size() && i < slots.length; i++) {
            PointCategory cat  = categories.get(i);
            int count = template == null ? 0 : countPlaced(session, cat);
            inv.setItem(slots[i], makeCategoryItem(cat, count, session, template));
        }

        // Deselect button (slot 31)
        inv.setItem(31, makeItem(DESEL_MAT, ChatColor.RED + "Deselect",
                "§7Clear current selection", "§8DESELECT"));

        player.openInventory(inv);
    }

    private void openCategoryPage(Player player, PointCategory cat) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE_PREFIX + cat.displayName);
        fillGlass(inv);

        MapSession session = MapBuilderPlugin.getInstance().getSession(player);
        MapTemplate template = session != null ? session.getState().getTemplate() : null;

        List<MapPointDefinition> defs = template != null
                ? template.byCategory(cat)
                : MapPointDefinition.byCategory(cat);

        // Populate starting at slot 10, skip glass border rows
        int[] bodySlots = buildBodySlots();
        for (int i = 0; i < defs.size() && i < bodySlots.length; i++) {
            MapPointDefinition def = defs.get(i);
            int placed = session != null ? session.countPoints(def.exportKey) : 0;
            boolean required = template != null && template.isRequired(def);
            inv.setItem(bodySlots[i], makePointItem(def, placed, required));
        }

        // Back button (slot 45) + Deselect (slot 49)
        inv.setItem(45, makeItem(BACK_MAT, ChatColor.YELLOW + "Back", "§7Return to category menu", "§8BACK"));
        inv.setItem(49, makeItem(DESEL_MAT, ChatColor.RED + "Deselect", "§7Clear selection", "§8DESELECT"));

        player.openInventory(inv);
    }

    // ── Event handler ────────────────────────────────────────────────────────

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        boolean isCategory = title.equals(TITLE_CATEGORY);
        boolean isPage     = title.startsWith(TITLE_PREFIX);
        if (!isCategory && !isPage) return;

        event.setCancelled(true);
        if (event.getCurrentItem() == null
                || event.getCurrentItem().getType() == Material.AIR
                || event.getCurrentItem().getType() == GLASS) return;

        Player player = (Player) event.getWhoClicked();
        MapSession session = MapBuilderPlugin.getInstance().getSession(player);
        if (session == null) { player.closeInventory(); return; }

        ItemMeta meta = event.getCurrentItem().getItemMeta();
        if (meta == null || !meta.hasLore()) return;
        String code = meta.getLore().get(meta.getLore().size() - 1); // last lore line is the code

        // ── Deselect ──
        if (code.equals("§8DESELECT")) {
            session.getState().deselectPoint();
            session.getState().clearPendingRegion();
            player.sendMessage(ChatColor.GRAY + "Selection cleared.");
            player.closeInventory();
            return;
        }

        // ── Back ──
        if (code.equals("§8BACK")) {
            player.closeInventory();
            Bukkit.getScheduler().runTask(MapBuilderPlugin.getInstance(),
                    () -> openCategoryMenu(player));
            return;
        }

        if (isCategory) {
            // code = "§8CAT:<name>" e.g. "§8CAT:SPAWNS"
            if (code.startsWith("§8CAT:")) {
                String catName = code.substring("§8CAT:".length());
                try {
                    PointCategory cat = PointCategory.valueOf(catName);
                    player.closeInventory();
                    Bukkit.getScheduler().runTask(MapBuilderPlugin.getInstance(),
                            () -> openCategoryPage(player, cat));
                } catch (IllegalArgumentException ignored) {}
            }
            return;
        }

        // isPage → code is "§8DEF:<exportKey>"
        if (code.startsWith("§8DEF:")) {
            String key = code.substring("§8DEF:".length());
            MapPointDefinition def = MapPointDefinition.fromExportKey(key);
            if (def != null) {
                session.getState().selectPoint(def);
                player.sendMessage(ChatColor.GREEN + "Selected: §e" + def.displayName
                        + " §7(" + def.kind.name() + ")");
                player.sendMessage(ChatColor.GRAY + "Right-Click with §6Point Tool§7 to place.");
            } else {
                player.sendMessage(ChatColor.RED + "Unknown point: " + key);
            }
            player.closeInventory();
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private ItemStack makeCategoryItem(PointCategory cat, int placed, MapSession session, MapTemplate template) {
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + cat.description);
        if (session != null && template != null) {
            long optional = template.byCategory(cat).size();
            long required = template.getRequiredPoints().stream().filter(d -> d.category == cat).count();
            lore.add("§7Points: §e" + placed + " placed");
            if (required > 0) lore.add("§cRequired in template: " + required);
        }
        lore.add("§8CAT:" + cat.name()); // code line (last)
        return makeItemWithLore(cat.icon, cat.displayName, lore);
    }

    private ItemStack makePointItem(MapPointDefinition def, int placed, boolean required) {
        List<String> lore = new ArrayList<>();
        lore.add("§7Kind: §e" + prettyKind(def.kind));
        lore.add("§7Placed: §f" + placed
                + (def.maxCount > 0 ? " §8/ " + def.maxCount : ""));
        if (required) lore.add("§cRequired by template");
        else           lore.add("§aOptional");
        lore.add("§8DEF:" + def.exportKey); // code line (last)
        return makeItemWithLore(def.icon, currentSelectionPrefix(def.displayName), lore);
    }

    private String currentSelectionPrefix(String name) { return ChatColor.YELLOW + name; }

    private String prettyKind(com.houzicore.mapbuilder.domain.PlacementKind k) {
        return switch (k) {
            case SINGLE      -> "Single — replaces existing";
            case MULTI       -> "Multi — unlimited";
            case PAIR_REGION -> "Pair Region — 2 corners";
            case DIRECTIONAL -> "Directional — stores facing";
        };
    }

    private int countPlaced(MapSession session, PointCategory cat) {
        int total = 0;
        for (MapPointDefinition def : MapPointDefinition.byCategory(cat)) {
            total += session.countPoints(def.exportKey);
        }
        return total;
    }

    private int[] buildBodySlots() {
        // 3×7 body inside a 54-slot inventory (rows 2,3,4; columns 2-8 → slots 10-16, 19-25, 28-34)
        int[] s = new int[21];
        int idx = 0;
        for (int row = 1; row <= 3; row++) {
            for (int col = 1; col <= 7; col++) {
                s[idx++] = row * 9 + col;
            }
        }
        return s;
    }

    private int[] centeredSlots(int count) {
        return switch (Math.max(1, Math.min(count, 4))) {
            case 1 -> new int[]{22};
            case 2 -> new int[]{21, 23};
            case 3 -> new int[]{20, 22, 24};
            default -> new int[]{20, 21, 23, 24};
        };
    }

    private void fillGlass(Inventory inv) {
        ItemStack glass = makeItem(GLASS, " ", null, null);
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) == null) inv.setItem(i, glass);
        }
    }

    private ItemStack makeItem(Material mat, String name, String lore1, String code) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta  = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore1 != null || code != null) {
            List<String> lore = new ArrayList<>();
            if (lore1 != null) lore.add(lore1);
            if (code  != null) lore.add(code);
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeItemWithLore(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta  = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
