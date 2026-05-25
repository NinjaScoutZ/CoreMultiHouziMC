package com.houzicore.lobby.hub.modules.farm;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.common.util.C;

import java.util.Arrays;

/**
 * Simple standalone GUI for the Farm Leaderboard.
 * Uses vanilla Inventory API directly (no ShopPageBase dependency).
 */
public class FarmLeaderboardPage implements InventoryHolder, Listener {

    private static final String[] TOP_NAMES  = {"Steve", "Alex", "Herobrine"};
    private static final int[]    TOP_SCORES = {38, 33, 27};

    private final Inventory _inv;

    public FarmLeaderboardPage(JavaPlugin plugin) {
        _inv = Bukkit.createInventory(this, 54, "§6§l🌾 Farm Leaderboard");
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        buildPage();
    }

    private void buildPage() {
        ItemStack glass = named(Material.ORANGE_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 9; i++) _inv.setItem(i, glass);
        for (int i = 45; i < 54; i++) _inv.setItem(i, glass);

        String[] medals = {"§6§l🥇", "§7§l🥈", "§c§l🥉"};
        for (int i = 0; i < Math.min(3, TOP_NAMES.length); i++) {
            String medal = i < 3 ? medals[i] : C.cGray + "#" + (i + 1);
            ItemStack head = named(Material.PLAYER_HEAD,
                medal + " " + C.cWhite + TOP_NAMES[i],
                C.cGray + "คะแนนสูงสุด: " + C.cYellow + TOP_SCORES[i] + " แต้ม",
                "",
                C.cGreen + "อันดับที่ " + (i + 1));
            _inv.setItem(10 + i * 2, head);
        }

        _inv.setItem(49, named(Material.COMPARATOR,
            C.cAqua + "§l📊 คะแนนของคุณ",
            C.cGray + "Best: " + C.cYellow + "กำลังโหลด..."));
    }

    public void open(Player player) {
        player.openInventory(_inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof FarmLeaderboardPage) {
            event.setCancelled(true);
        }
    }

    @Override
    public Inventory getInventory() { return _inv; }

    private static ItemStack named(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }
}
