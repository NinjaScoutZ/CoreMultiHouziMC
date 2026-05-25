package com.houzicore.mapbuilder.tool;

import com.houzicore.mapbuilder.MapBuilderPlugin;
import com.houzicore.mapbuilder.MapSession;
import com.houzicore.mapbuilder.VisualManager;
import com.houzicore.mapbuilder.session.PlacementAction;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Handles the Boundary Tool (slot 2, Wooden Axe):
 *   Left-Click  BLOCK → set Min corner
 *   Right-Click BLOCK → set Max corner
 *
 * No hidden modes.  Visual feedback via VisualManager.
 */
public class BoundaryToolHandler implements Listener {

    public static final String WAND_NAME = ChatColor.AQUA + "Boundary Tool";

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        if (!isHoldingTool(player)) return;

        MapSession session = MapBuilderPlugin.getInstance().getSession(player);
        if (session == null) return;
        event.setCancelled(true);

        if (event.getClickedBlock() == null) return;
        Location clicked = event.getClickedBlock().getLocation();
        Action action = event.getAction();

        if (action == Action.LEFT_CLICK_BLOCK) {
            Location prev = session.getMinBoundary();
            session.setMinBoundary(clicked);
            session.getState().recordAction(
                    new PlacementAction(PlacementAction.ActionType.SET_MIN, clicked, prev));
            VisualManager.getInstance().updateBoundaryVisual(session);
            player.sendMessage(ChatColor.AQUA + "Min boundary set: §f"
                    + clicked.getBlockX() + ", " + clicked.getBlockY() + ", " + clicked.getBlockZ());

        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            Location prev = session.getMaxBoundary();
            session.setMaxBoundary(clicked);
            session.getState().recordAction(
                    new PlacementAction(PlacementAction.ActionType.SET_MAX, clicked, prev));
            VisualManager.getInstance().updateBoundaryVisual(session);
            player.sendMessage(ChatColor.AQUA + "Max boundary set: §f"
                    + clicked.getBlockX() + ", " + clicked.getBlockY() + ", " + clicked.getBlockZ());
        }
    }

    public static boolean isHoldingTool(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        return item != null && item.hasItemMeta()
                && item.getItemMeta().hasDisplayName()
                && item.getItemMeta().getDisplayName().equals(WAND_NAME);
    }
}
