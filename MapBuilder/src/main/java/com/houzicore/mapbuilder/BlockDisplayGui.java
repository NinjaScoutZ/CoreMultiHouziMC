package com.houzicore.mapbuilder;

import com.houzicore.mapbuilder.tool.DisplayToolHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Paginated GUI for browsing Minecraft block materials and spawning
 * individual Block Display entities for map building.
 */
public class BlockDisplayGui implements Listener {

    private static BlockDisplayGui _instance;

    private static final String TITLE_PREFIX = ChatColor.GOLD + "" + ChatColor.BOLD + "Block Display " + ChatColor.RESET + ChatColor.GRAY + "▸ ";
    private static final int ITEMS_PER_PAGE = 45; // 5 rows of items, bottom row for navigation

    private final List<Material> _allBlocks;
    private final Map<UUID, Integer> _playerPages = new HashMap<>();
    private final Map<UUID, String> _playerFilters = new HashMap<>();
    private final Map<UUID, Material> _selectedMaterials = new HashMap<>();

    public BlockDisplayGui() {
        _instance = this;

        // Collect all solid block materials, sorted alphabetically
        _allBlocks = new ArrayList<>();
        for (Material mat : Material.values()) {
            if (mat.isBlock() && !mat.isAir() && mat.isItem()) {
                _allBlocks.add(mat);
            }
        }
        _allBlocks.sort((a, b) -> a.name().compareTo(b.name()));

        Bukkit.getPluginManager().registerEvents(this, MapBuilderPlugin.getInstance());
    }

    public static BlockDisplayGui getInstance() {
        return _instance;
    }

    /**
     * Open the Block Display browser GUI for the given player.
     * @param player The builder
     * @param filter Optional text filter (null for no filter)
     */
    public void openGui(Player player, String filter) {
        if (filter != null && !filter.isEmpty()) {
            _playerFilters.put(player.getUniqueId(), filter.toUpperCase());
        }
        _playerPages.put(player.getUniqueId(), 0);
        buildAndOpen(player);
    }

    /**
     * Get the currently selected Block Display material for a player.
     */
    public Material getSelectedMaterial(Player player) {
        return _selectedMaterials.get(player.getUniqueId());
    }

    /**
     * Set the selected Block Display material for a player.
     */
    public void setSelectedMaterial(Player player, Material mat) {
        _selectedMaterials.put(player.getUniqueId(), mat);
    }

    private List<Material> getFilteredList(Player player) {
        String filter = _playerFilters.get(player.getUniqueId());
        if (filter == null || filter.isEmpty()) {
            return _allBlocks;
        }
        return _allBlocks.stream()
                .filter(m -> m.name().contains(filter))
                .collect(Collectors.toList());
    }

    private void buildAndOpen(Player player) {
        List<Material> filtered = getFilteredList(player);
        int page = _playerPages.getOrDefault(player.getUniqueId(), 0);
        int maxPage = Math.max(0, (filtered.size() - 1) / ITEMS_PER_PAGE);
        if (page > maxPage) page = maxPage;
        _playerPages.put(player.getUniqueId(), page);

        String filterText = _playerFilters.getOrDefault(player.getUniqueId(), "");
        String titleSuffix = filterText.isEmpty() ? "All Blocks" : "\"" + filterText.toLowerCase() + "\"";
        String title = TITLE_PREFIX + titleSuffix + " §8[" + (page + 1) + "/" + (maxPage + 1) + "]";

        // Truncate title to 32 chars for Bukkit inventory limit
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }

        Inventory inv = Bukkit.createInventory(null, 54, title);

        // Fill items for current page
        int startIndex = page * ITEMS_PER_PAGE;
        for (int i = 0; i < ITEMS_PER_PAGE && (startIndex + i) < filtered.size(); i++) {
            Material mat = filtered.get(startIndex + i);
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();

            String displayName = formatMaterialName(mat);
            meta.setDisplayName(ChatColor.YELLOW + displayName);

            // Lore in HouziCore style
            Material selected = _selectedMaterials.get(player.getUniqueId());
            boolean isSelected = mat == selected;

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.DARK_GRAY + "──────────────────────");
            lore.add(ChatColor.GRAY + "Block Display Entity");
            lore.add(ChatColor.GRAY + "Material: " + ChatColor.WHITE + mat.name());
            lore.add("");
            if (isSelected) {
                lore.add(ChatColor.GREEN + "✔ Currently Selected");
            } else {
                lore.add(ChatColor.AQUA + "Click to select & place");
            }
            lore.add(ChatColor.DARK_GRAY + "──────────────────────");
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(i, item);
        }

        // Bottom navigation row (slots 45-53)
        // Glass pane borders
        ItemStack border = createNavItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 45; i <= 53; i++) {
            inv.setItem(i, border);
        }

        // Previous page
        if (page > 0) {
            inv.setItem(45, createNavItem(Material.ARROW, ChatColor.AQUA + "◀ Previous Page"));
        }

        // Info center
        inv.setItem(49, createNavItem(Material.BOOK,
                ChatColor.GOLD + "Page " + (page + 1) + "/" + (maxPage + 1),
                ChatColor.GRAY + "Total: " + ChatColor.WHITE + filtered.size() + " blocks",
                ChatColor.GRAY + "Use " + ChatColor.YELLOW + "/mb display <filter>" + ChatColor.GRAY + " to search"));

        // Next page
        if (page < maxPage) {
            inv.setItem(53, createNavItem(Material.ARROW, ChatColor.AQUA + "Next Page ▶"));
        }

        // Clear filter button
        if (!filterText.isEmpty()) {
            inv.setItem(47, createNavItem(Material.BARRIER, ChatColor.RED + "Clear Filter"));
        }

        player.openInventory(inv);
    }

    private ItemStack createNavItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore.length > 0) {
            meta.setLore(Arrays.asList(lore));
        }
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String viewTitle = event.getView().getTitle();
        if (viewTitle == null || !viewTitle.startsWith(ChatColor.GOLD + "" + ChatColor.BOLD + "Block Display")) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        MapSession session = MapBuilderPlugin.getInstance().getSession(player);
        if (session == null) {
            player.closeInventory();
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getRawSlot();

        // Navigation clicks (bottom row)
        if (slot >= 45 && slot <= 53) {
            if (slot == 45) {
                // Previous page
                int page = _playerPages.getOrDefault(player.getUniqueId(), 0);
                if (page > 0) {
                    _playerPages.put(player.getUniqueId(), page - 1);
                    buildAndOpen(player);
                }
            } else if (slot == 53) {
                // Next page
                int page = _playerPages.getOrDefault(player.getUniqueId(), 0);
                _playerPages.put(player.getUniqueId(), page + 1);
                buildAndOpen(player);
            } else if (slot == 47 && clicked.getType() == Material.BARRIER) {
                // Clear filter
                _playerFilters.remove(player.getUniqueId());
                _playerPages.put(player.getUniqueId(), 0);
                buildAndOpen(player);
            }
            return;
        }

        // Material selection click (slots 0-44)
        if (slot >= 0 && slot < ITEMS_PER_PAGE) {
            Material mat = clicked.getType();
            if (!mat.isBlock()) return;

            // Apply to BuilderSessionState via DisplayToolHandler — NOT ToolManager
            _selectedMaterials.put(player.getUniqueId(), mat);
            DisplayToolHandler.applyMaterialSelection(player, mat);

            player.closeInventory();
            player.sendMessage(ChatColor.GREEN + "Selected: §d" + formatMaterialName(mat)
                    + " §7— Right-Click with §d" + DisplayToolHandler.WAND_NAME + " §7to place.");
        }
    }

    /**
     * Formats a Material enum name into a readable title.
     * e.g. STONE_BRICKS -> Stone Bricks
     */
    public static String formatMaterialName(Material mat) {
        String[] words = mat.name().toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));
        }
        return sb.toString();
    }
}
