package com.houzicore.mapparser.tools;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.houzicore.mapparser.MapParserPlugin;
import com.houzicore.mapparser.MapData;
import com.houzicore.arcade.GameMapRequirements;
import com.houzicore.arcade.GameMapRequirements.GameReqs;
import com.houzicore.arcade.GameMapRequirements.MarkerReq;
import com.houzicore.arcade.GameMapRequirements.TeamReq;
import com.houzicore.shared.common.util.C;

import java.util.ArrayList;
import java.util.List;

public class MapToolManager implements Listener {

    private final MapParserPlugin plugin;
    private static final String GUI_TITLE = "🛠️ Map Builder Tools";

    public MapToolManager(MapParserPlugin plugin) {
        this.plugin = plugin;
    }

    public void openToolMenu(Player player) {
        String worldName = player.getWorld().getName();
        if (worldName.equals("world_lobby")) {
            player.sendMessage(C.cRed + "Cannot use tools in Lobby.");
            return;
        }

        MapData data = plugin.getData(worldName);
        if (data.MapGameType == null || !GameMapRequirements.hasRequirements(data.MapGameType)) {
            player.sendMessage(C.cRed + "MapGameType must be set to a valid game first! Use: /map gametype <type>");
            return;
        }

        GameReqs reqs = GameMapRequirements.getRequirements(data.MapGameType);
        
        // 54 slots (6 rows)
        Inventory inv = Bukkit.createInventory(null, 54, GUI_TITLE);

        // -- Map Settings (Slot 4) --
        inv.setItem(4, createItem(Material.COMPASS, "§eMap Settings", "§7Click to get the Map Tool Wand"));

        // -- Corner Markers (Slot 9) --
        inv.setItem(9, createMarkerItem(Material.WHITE_WOOL, "§fMap Corner", "§7(White Wool + Gold Plate)"));

        // -- Team Spawns (Row 2) --
        int slot = 18;
        for (TeamReq team : reqs.teams()) {
            Material wool = getWoolMaterial(team.color());
            if (wool != null) {
                inv.setItem(slot++, createMarkerItem(wool, "§eTeam Spawn: §b" + team.teamName(), "§7(" + team.color() + " Wool + Gold Plate)"));
            }
        }

        // -- Data Locs (Row 4) --
        slot = 36;
        for (MarkerReq mReq : reqs.dataLocs()) {
            Material wool = getWoolMaterial(mReq.color());
            if (wool != null) {
                inv.setItem(slot++, createMarkerItem(wool, "§dData: §f" + mReq.color(), "§7(" + mReq.color() + " Wool + Iron Plate)", "§8" + mReq.description()));
            }
        }

        // -- Custom Locs (Row 5) --
        slot = 45;
        for (MarkerReq mReq : reqs.customLocs()) {
            inv.setItem(slot++, createMarkerItem(Material.SPONGE, "§aCustom: §f" + mReq.color(), "§7(Sponge + Sign)", "§8" + mReq.description()));
        }

        player.openInventory(inv);
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> loreList = new ArrayList<>();
        for (String l : lore) loreList.add(l);
        meta.setLore(loreList);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createMarkerItem(Material mat, String name, String... lore) {
        ItemStack item = createItem(mat, name, lore);
        // Add a hidden tag to identify it as a smart marker block
        ItemMeta meta = item.getItemMeta();
        List<String> loreList = meta.getLore();
        if (loreList == null) loreList = new ArrayList<>();
        loreList.add("§0[SmartMarker]");
        meta.setLore(loreList);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked != null && clicked.getType() != Material.AIR) {
            // Give 64 of the clicked block
            ItemStack toGive = clicked.clone();
            toGive.setAmount(64);
            player.getInventory().addItem(toGive);
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_PICKUP, 1f, 1f);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack hand = event.getItemInHand();
        if (hand == null || !hand.hasItemMeta() || !hand.getItemMeta().hasLore()) return;

        List<String> lore = hand.getItemMeta().getLore();
        if (!lore.contains("§0[SmartMarker]")) return;

        // Smart Marker placed! We need to place the plate/sign on top.
        Block placed = event.getBlockPlaced();
        Block top = placed.getRelative(BlockFace.UP);

        if (top.getType() != Material.AIR) {
            event.getPlayer().sendMessage(C.cRed + "No space above block to place the marker component!");
            event.setCancelled(true);
            return;
        }

        String itemName = hand.getItemMeta().getDisplayName();

        // Determine what to place on top
        if (itemName.contains("Corner") || itemName.contains("Team Spawn")) {
            top.setType(Material.LIGHT_WEIGHTED_PRESSURE_PLATE);
        } 
        else if (itemName.contains("Data:")) {
            top.setType(Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
        }
        else if (itemName.contains("Custom:")) {
            top.setType(Material.OAK_SIGN);
            if (top.getState() instanceof Sign sign) {
                // Extract the custom name (e.g. "§aCustom: §fTerminal" -> "Terminal")
                String text = itemName.replaceAll("§[0-9a-fk-or]", "").replace("Custom: ", "").trim();
                sign.setLine(0, text);
                sign.update(true);
            }
        }
        
        // Let the MarkerFeedbackListener catch the plate/sign placement by simulating it?
        // Actually MarkerFeedbackListener only catches BlockPlaceEvent, which won't fire for the top block.
        // We'll just play a sound directly here to ensure they get feedback.
        event.getPlayer().playSound(placed.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
    }

    private Material getWoolMaterial(String colorName) {
        try {
            return Material.valueOf(colorName.toUpperCase() + "_WOOL");
        } catch (Exception e) {
            return null; // Some generic string
        }
    }
}
