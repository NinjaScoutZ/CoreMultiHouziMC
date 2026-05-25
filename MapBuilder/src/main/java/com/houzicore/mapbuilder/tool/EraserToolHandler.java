package com.houzicore.mapbuilder.tool;

import com.houzicore.mapbuilder.MapBuilderPlugin;
import com.houzicore.mapbuilder.MapSession;
import com.houzicore.mapbuilder.VisualManager;
import com.houzicore.mapbuilder.domain.MapPointDefinition;
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

import java.util.Map;
import java.util.List;

/**
 * Handles the Eraser (slot 4, Iron Hoe):
 *   Left-Click  → delete the nearest data point within 4 blocks
 *   Right-Click → inspect the nearest data point (name + coords, no deletion)
 *
 * Uses proximity — no need to aim exactly at the block under the marker.
 */
public class EraserToolHandler implements Listener {

    public static final String WAND_NAME = ChatColor.RED + "Eraser";

    private static final double ERASE_RADIUS    = 4.0;
    private static final double ERASE_RADIUS_SQ = ERASE_RADIUS * ERASE_RADIUS;

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        if (!isHoldingTool(player)) return;

        MapSession session = MapBuilderPlugin.getInstance().getSession(player);
        if (session == null) return;
        event.setCancelled(true);

        Action action = event.getAction();
        Location eye = player.getEyeLocation();

        if (action == Action.LEFT_CLICK_BLOCK || action == Action.LEFT_CLICK_AIR) {
            eraseNearest(player, session, eye);
        } else if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
            inspectNearest(player, session, eye);
        }
    }

    private void eraseNearest(Player player, MapSession session, Location eye) {
        NearestResult result = findNearest(session, eye);
        if (result == null) {
            player.sendMessage(ChatColor.GRAY + "No point within " + ERASE_RADIUS + " blocks.");
            return;
        }
        session.removeDataPoint(result.exportKey, result.loc);
        VisualManager.getInstance().removeVisual(result.loc);

        // Record for undo
        MapPointDefinition def = MapPointDefinition.fromExportKey(result.exportKey);
        session.getState().recordAction(new PlacementAction(
                PlacementAction.ActionType.DELETE, def, result.loc));

        String label = def != null ? def.displayName : result.exportKey;
        player.sendMessage(ChatColor.RED + "Erased: " + ChatColor.WHITE + label
                + ChatColor.GRAY + " [" + result.loc.getBlockX() + ", "
                + result.loc.getBlockY() + ", " + result.loc.getBlockZ() + "]");
    }

    private void inspectNearest(Player player, MapSession session, Location eye) {
        NearestResult result = findNearest(session, eye);
        if (result == null) {
            player.sendMessage(ChatColor.GRAY + "No point within " + ERASE_RADIUS + " blocks.");
            return;
        }
        MapPointDefinition def = MapPointDefinition.fromExportKey(result.exportKey);
        String label    = def != null ? def.displayName : result.exportKey;
        String category = def != null ? def.category.displayName : "§7Unknown";
        player.sendMessage(ChatColor.YELLOW + "─── Inspect ───────────────────────");
        player.sendMessage(ChatColor.WHITE  + "  Type:     " + ChatColor.AQUA  + label);
        player.sendMessage(ChatColor.WHITE  + "  Category: " + category);
        player.sendMessage(ChatColor.WHITE  + "  Key:      " + ChatColor.GRAY  + result.exportKey);
        player.sendMessage(ChatColor.WHITE  + "  Coords:   " + ChatColor.GREEN
                + result.loc.getBlockX() + ", " + result.loc.getBlockY() + ", " + result.loc.getBlockZ());
        player.sendMessage(ChatColor.YELLOW + "───────────────────────────────────");
    }

    private NearestResult findNearest(MapSession session, Location origin) {
        String bestKey = null;
        Location bestLoc = null;
        double bestDist = ERASE_RADIUS_SQ;

        for (Map.Entry<String, List<Location>> entry : session.getDataPoints().entrySet()) {
            for (Location loc : entry.getValue()) {
                if (!loc.getWorld().equals(origin.getWorld())) continue;
                double d = loc.distanceSquared(origin);
                if (d < bestDist) {
                    bestDist = d;
                    bestKey  = entry.getKey();
                    bestLoc  = loc;
                }
            }
        }

        if (bestKey == null) return null;
        return new NearestResult(bestKey, bestLoc);
    }

    private record NearestResult(String exportKey, Location loc) {}

    public static boolean isHoldingTool(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        return item != null && item.hasItemMeta()
                && item.getItemMeta().hasDisplayName()
                && item.getItemMeta().getDisplayName().equals(WAND_NAME);
    }
}
