package com.houzicore.mapbuilder.tool;

import com.houzicore.mapbuilder.MapBuilderPlugin;
import com.houzicore.mapbuilder.MapSession;
import com.houzicore.mapbuilder.VisualManager;
import com.houzicore.mapbuilder.domain.MapPointDefinition;
import com.houzicore.mapbuilder.domain.PlacementKind;
import com.houzicore.mapbuilder.gui.PointPaletteGui;
import com.houzicore.mapbuilder.session.BuilderSessionState;
import com.houzicore.mapbuilder.session.PlacementAction;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Handles all interactions with the Point Tool (slot 1, Blaze Rod).
 *
 * Interaction model:
 *   Right-Click AIR                → open palette (always)
 *   Right-Click BLOCK, no select   → open palette
 *   Right-Click BLOCK, has select  → place point (PlacementKind logic)
 *   Shift + Right-Click            → open palette regardless (change selection)
 *   Drop (Q)                       → deselect + cancel drop
 *   Left-Click                     → nothing (use Eraser for deletion)
 */
public class PointToolHandler implements Listener {

    public static final String WAND_NAME = ChatColor.GOLD + "Point Tool";

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        if (!isHoldingTool(player)) return;

        MapSession session = MapBuilderPlugin.getInstance().getSession(player);
        if (session == null) return;
        event.setCancelled(true);

        Action action = event.getAction();
        BuilderSessionState state = session.getState();

        // ── Always open palette on right-click air OR sneak+right ──
        if (action == Action.RIGHT_CLICK_AIR
                || (action == Action.RIGHT_CLICK_BLOCK && player.isSneaking())
                || (action == Action.RIGHT_CLICK_BLOCK && !state.hasSelectedPoint())) {
            PointPaletteGui.getInstance().open(player, null);
            return;
        }

        // ── Right-click block with selection → place ──
        if (action == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            if (!state.hasSelectedPoint()) {
                PointPaletteGui.getInstance().open(player, null);
                return;
            }
            Location target = event.getClickedBlock().getLocation().add(0.5, 1.0, 0.5);
            handlePlace(player, session, state, target);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!isHoldingTool(player)) return;
        MapSession session = MapBuilderPlugin.getInstance().getSession(player);
        if (session == null) return;

        event.setCancelled(true); // don't drop the wand
        BuilderSessionState state = session.getState();

        if (state.hasPendingRegionAnchor()) {
            state.clearPendingRegion();
            player.sendMessage(ChatColor.YELLOW + "Region placement cancelled.");
        } else if (state.hasSelectedPoint()) {
            String prev = state.getSelectedPoint().displayName;
            state.deselectPoint();
            player.sendMessage(ChatColor.GRAY + "Deselected: " + ChatColor.WHITE + prev);
        } else {
            player.sendMessage(ChatColor.GRAY + "Nothing selected.");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    private void handlePlace(Player player, MapSession session, BuilderSessionState state, Location target) {
        MapPointDefinition def = state.getSelectedPoint();
        PlacementKind kind     = def.kind;

        switch (kind) {
            case SINGLE -> placeSingle(player, session, state, def, target);
            case MULTI  -> placeMulti(player, session, state, def, target);
            case DIRECTIONAL -> placeDirectional(player, session, state, def, target);
            case PAIR_REGION -> placePairRegion(player, session, state, def, target);
        }
    }

    private void placeSingle(Player player, MapSession session, BuilderSessionState state,
                              MapPointDefinition def, Location target) {
        int existing = session.countPoints(def.exportKey);
        if (existing > 0) {
            // Replace: remove old visual(s), then place
            session.getDataPoints().get(def.exportKey).forEach(VisualManager.getInstance()::removeVisual);
            session.getDataPoints().get(def.exportKey).clear();
        }
        doPlace(player, session, state, def, target);
        if (existing > 0) {
            player.sendMessage(ChatColor.YELLOW + "Replaced: " + def.displayName);
        }
    }

    private void placeMulti(Player player, MapSession session, BuilderSessionState state,
                             MapPointDefinition def, Location target) {
        doPlace(player, session, state, def, target);
    }

    private void placeDirectional(Player player, MapSession session, BuilderSessionState state,
                                   MapPointDefinition def, Location target) {
        // Store yaw/pitch in the location
        target.setYaw(player.getLocation().getYaw());
        target.setPitch(player.getLocation().getPitch());
        doPlace(player, session, state, def, target);
    }

    private void placePairRegion(Player player, MapSession session, BuilderSessionState state,
                                  MapPointDefinition def, Location target) {
        if (!state.hasPendingRegionAnchor()) {
            // First click — set anchor
            state.setPendingRegion(def, target);
            VisualManager.getInstance().spawnVisual(player.getUniqueId(), def.exportKey, target);
            player.sendMessage(ChatColor.YELLOW + def.displayName + " §7— "
                    + ChatColor.GREEN + "Point 1 set. Right-Click Point 2.");
        } else if (state.getPendingRegionType() == def) {
            // Second click — commit both
            Location anchor = state.getPendingRegionAnchor();
            doPlace(player, session, state, def, anchor);
            doPlace(player, session, state, def, target);
            state.clearPendingRegion();
            player.sendMessage(ChatColor.GREEN + def.displayName + " §7— §aZone committed!");
        } else {
            // Started a different region in between — cancel old, start new
            VisualManager.getInstance().removeVisual(state.getPendingRegionAnchor());
            state.setPendingRegion(def, target);
            VisualManager.getInstance().spawnVisual(player.getUniqueId(), def.exportKey, target);
            player.sendMessage(ChatColor.YELLOW + "Switched to: " + def.displayName
                    + " §7— §ePoint 1 set. Right-Click Point 2.");
        }
    }

    private void doPlace(Player player, MapSession session, BuilderSessionState state,
                          MapPointDefinition def, Location loc) {
        session.addDataPoint(def.exportKey, loc);
        VisualManager.getInstance().spawnVisual(player.getUniqueId(), def.exportKey, loc);

        // Record for undo
        state.recordAction(new PlacementAction(
                PlacementAction.ActionType.PLACE, def, loc));

        int count = session.countPoints(def.exportKey);
        player.sendMessage(ChatColor.GREEN + "Placed: " + ChatColor.YELLOW + def.displayName
                + ChatColor.GRAY + " (" + count + " total)"
                + " §8[" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + "]");
    }

    // ─────────────────────────────────────────────────────────────────────────

    public static boolean isHoldingTool(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        return item != null && item.hasItemMeta()
                && item.getItemMeta().hasDisplayName()
                && item.getItemMeta().getDisplayName().equals(WAND_NAME);
    }
}
