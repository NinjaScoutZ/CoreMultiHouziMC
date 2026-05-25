package com.houzicore.mapbuilder.tool;

import com.houzicore.mapbuilder.MapBuilderPlugin;
import com.houzicore.mapbuilder.MapSession;
import com.houzicore.mapbuilder.VisualManager;
import com.houzicore.mapbuilder.session.BuilderSessionState;
import com.houzicore.mapbuilder.session.PlacementAction;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Handles the Undo / Redo tool (slot 7, Clock):
 *   Left-Click  → undo last action
 *   Right-Click → redo last undone action
 *
 * Shared logic is exposed as static methods so /mb undo and /mb redo
 * in MapBuilderCommand can reuse the same code path.
 */
public class UndoRedoToolHandler implements Listener {

    public static final String WAND_NAME = ChatColor.YELLOW + "Undo / Redo";

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        if (!isHoldingTool(player)) return;

        MapSession session = MapBuilderPlugin.getInstance().getSession(player);
        if (session == null) return;
        event.setCancelled(true);

        Action action = event.getAction();
        if (action == Action.LEFT_CLICK_BLOCK || action == Action.LEFT_CLICK_AIR) {
            performUndo(player, session);
        } else if (action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR) {
            performRedo(player, session);
        }
    }

    // ── Static helpers reused by MapBuilderCommand ───────────────────────────

    public static void performUndo(Player player, MapSession session) {
        BuilderSessionState state = session.getState();
        if (!state.canUndo()) {
            player.sendMessage(ChatColor.RED + "Nothing to undo.");
            return;
        }
        PlacementAction act = state.popUndo();
        reverseAction(player, session, act);
    }

    public static void performRedo(Player player, MapSession session) {
        BuilderSessionState state = session.getState();
        if (!state.canRedo()) {
            player.sendMessage(ChatColor.RED + "Nothing to redo.");
            return;
        }
        PlacementAction act = state.popRedo();
        applyAction(player, session, act);
    }

    // ─────────────────────────────────────────────────────────────────────────

    private static void reverseAction(Player player, MapSession session, PlacementAction act) {
        switch (act.getType()) {
            case PLACE -> {
                String key = act.getRawKey();
                if (key != null) {
                    session.removeDataPoint(key, act.getLocation());
                    VisualManager.getInstance().removeVisual(act.getLocation());
                    String label = act.getDefinition() != null ? act.getDefinition().displayName : key;
                    player.sendMessage(ChatColor.YELLOW + "⟲ Undid place: " + label);
                }
            }
            case DELETE -> {
                String key = act.getRawKey();
                if (key != null) {
                    session.addDataPoint(key, act.getLocation());
                    VisualManager.getInstance().spawnVisual(player.getUniqueId(), key, act.getLocation());
                    String label = act.getDefinition() != null ? act.getDefinition().displayName : key;
                    player.sendMessage(ChatColor.YELLOW + "⟲ Undid erase: " + label);
                }
            }
            case SET_MIN -> {
                session.setMinBoundary(act.getPreviousLocation());
                VisualManager.getInstance().updateBoundaryVisual(session);
                player.sendMessage(ChatColor.YELLOW + "⟲ Undid: set min boundary");
            }
            case SET_MAX -> {
                session.setMaxBoundary(act.getPreviousLocation());
                VisualManager.getInstance().updateBoundaryVisual(session);
                player.sendMessage(ChatColor.YELLOW + "⟲ Undid: set max boundary");
            }
        }
    }

    private static void applyAction(Player player, MapSession session, PlacementAction act) {
        switch (act.getType()) {
            case PLACE -> {
                String key = act.getRawKey();
                if (key != null) {
                    session.addDataPoint(key, act.getLocation());
                    VisualManager.getInstance().spawnVisual(player.getUniqueId(), key, act.getLocation());
                    String label = act.getDefinition() != null ? act.getDefinition().displayName : key;
                    player.sendMessage(ChatColor.GREEN + "⟳ Redid place: " + label);
                }
            }
            case DELETE -> {
                String key = act.getRawKey();
                if (key != null) {
                    session.removeDataPoint(key, act.getLocation());
                    VisualManager.getInstance().removeVisual(act.getLocation());
                    String label = act.getDefinition() != null ? act.getDefinition().displayName : key;
                    player.sendMessage(ChatColor.GREEN + "⟳ Redid erase: " + label);
                }
            }
            case SET_MIN -> {
                session.setMinBoundary(act.getLocation());
                VisualManager.getInstance().updateBoundaryVisual(session);
                player.sendMessage(ChatColor.GREEN + "⟳ Redid: set min boundary");
            }
            case SET_MAX -> {
                session.setMaxBoundary(act.getLocation());
                VisualManager.getInstance().updateBoundaryVisual(session);
                player.sendMessage(ChatColor.GREEN + "⟳ Redid: set max boundary");
            }
        }
    }

    public static boolean isHoldingTool(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        return item != null && item.hasItemMeta()
                && item.getItemMeta().hasDisplayName()
                && item.getItemMeta().getDisplayName().equals(WAND_NAME);
    }
}
