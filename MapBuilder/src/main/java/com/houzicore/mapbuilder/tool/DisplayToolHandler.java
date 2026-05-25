package com.houzicore.mapbuilder.tool;

import com.houzicore.mapbuilder.BlockDisplayGui;
import com.houzicore.mapbuilder.MapBuilderPlugin;
import com.houzicore.mapbuilder.MapSession;
import com.houzicore.mapbuilder.VisualManager;
import com.houzicore.mapbuilder.session.BuilderSessionState;
import com.houzicore.mapbuilder.session.PlacementAction;
import com.houzicore.mapbuilder.domain.MapPointDefinition;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Handles the Display Tool (slot 3, Breeze Rod):
 *   Shift + Right-Click → browse materials (BlockDisplayGui)
 *   Right-Click BLOCK   → place Block Display using selectedDisplayMaterial
 *   Left-Click          → nothing (use Eraser)
 *
 * Uses BuilderSessionState#selectedDisplayMaterial — NEVER touches selectedPoint.
 */
public class DisplayToolHandler implements Listener {

    public static final String WAND_NAME = ChatColor.LIGHT_PURPLE + "Display Tool";

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

        // Shift+Right → browse
        if ((action == Action.RIGHT_CLICK_BLOCK || action == Action.RIGHT_CLICK_AIR)
                && player.isSneaking()) {
            BlockDisplayGui.getInstance().openGui(player, null);
            return;
        }

        // Right-Click BLOCK → place
        if (action == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null) {
            String displayKey = null;

            if (state.getSelectedDisplayModel() != null) {
                displayKey = "DISPLAY_MODEL:" + state.getSelectedDisplayModel();
            } else if (state.getSelectedDisplayMaterial() != null) {
                displayKey = "BLOCK_DISPLAY:" + state.getSelectedDisplayMaterial().name();
            }

            if (displayKey == null) {
                BlockDisplayGui.getInstance().openGui(player, null);
                return;
            }

            Location loc = event.getClickedBlock().getLocation().add(0.5, 1.0, 0.5);
            session.addDataPoint(displayKey, loc);
            VisualManager.getInstance().spawnVisual(player.getUniqueId(), displayKey, loc);

            // Record for undo — use rawKey constructor so reversal knows which data key to remove
            state.recordAction(new PlacementAction(PlacementAction.ActionType.PLACE, displayKey, loc));

            String label = state.getSelectedDisplayModel() != null
                    ? "Model: " + state.getSelectedDisplayModel()
                    : BlockDisplayGui.formatMaterialName(state.getSelectedDisplayMaterial());
            player.sendMessage(ChatColor.GREEN + "Placed §d" + label
                    + ChatColor.GRAY + " [" + loc.getBlockX() + ", " + loc.getBlockY()
                    + ", " + loc.getBlockZ() + "]");
        }
    }

    /** Called by BlockDisplayGui after player picks a material. */
    public static void applyMaterialSelection(Player player, Material mat) {
        MapSession session = MapBuilderPlugin.getInstance().getSession(player);
        if (session == null) return;
        session.getState().selectDisplayMaterial(mat);
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Display material: §f"
                + BlockDisplayGui.formatMaterialName(mat));
    }

    /** Called by PointPaletteGui / elsewhere after player picks a display model. */
    public static void applyModelSelection(Player player, String modelId) {
        MapSession session = MapBuilderPlugin.getInstance().getSession(player);
        if (session == null) return;
        session.getState().selectDisplayModel(modelId);
        player.sendMessage(ChatColor.LIGHT_PURPLE + "Display model: §f" + modelId);
    }

    public static boolean isHoldingTool(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        return item != null && item.hasItemMeta()
                && item.getItemMeta().hasDisplayName()
                && item.getItemMeta().getDisplayName().equals(WAND_NAME);
    }
}
