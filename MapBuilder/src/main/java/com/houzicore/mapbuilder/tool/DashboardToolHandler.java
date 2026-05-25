package com.houzicore.mapbuilder.tool;

import com.houzicore.mapbuilder.MapBuilderPlugin;
import com.houzicore.mapbuilder.MapSession;
import com.houzicore.mapbuilder.gui.DashboardGui;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class DashboardToolHandler implements Listener {

    public static final String WAND_NAME = ChatColor.DARK_PURPLE + "Builder Dashboard";

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        if (!isHoldingTool(player)) return;
        
        MapSession session = MapBuilderPlugin.getInstance().getSession(player);
        if (session == null) return;
        
        event.setCancelled(true);
        DashboardGui.getInstance().open(player);
    }

    public static boolean isHoldingTool(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        return item != null && item.hasItemMeta()
                && item.getItemMeta().hasDisplayName()
                && item.getItemMeta().getDisplayName().equals(WAND_NAME);
    }
}
