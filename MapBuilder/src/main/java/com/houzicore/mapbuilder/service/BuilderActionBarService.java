package com.houzicore.mapbuilder.service;

import com.houzicore.mapbuilder.MapBuilderPlugin;
import com.houzicore.mapbuilder.MapSession;
import com.houzicore.mapbuilder.domain.MapPointDefinition;
import com.houzicore.mapbuilder.session.BuilderSessionState;
import com.houzicore.mapbuilder.tool.BoundaryToolHandler;
import com.houzicore.mapbuilder.tool.DisplayToolHandler;
import com.houzicore.mapbuilder.tool.EraserToolHandler;
import com.houzicore.mapbuilder.tool.PointToolHandler;
import com.houzicore.shared.common.actionbar.ActionBarChannel;
import com.houzicore.shared.common.actionbar.ActionBarService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * Ticks every 2 seconds (40 ticks) and sends a single ActionBar message
 * to every player who is in an active map-builder session.
 *
 * The message adapts to which tool the player is currently holding.
 */
public final class BuilderActionBarService {

    private static BuilderActionBarService instance;
    private BukkitTask task;

    private BuilderActionBarService() {}

    public static BuilderActionBarService getInstance() {
        if (instance == null) instance = new BuilderActionBarService();
        return instance;
    }

    public void start(Plugin plugin) {
        if (task != null && !task.isCancelled()) return;
        task = new BukkitRunnable() {
            @Override public void run() { tick(); }
        }.runTaskTimer(plugin, 20L, 40L); // tick every 2 sec
    }

    public void stop() {
        if (task != null) { task.cancel(); task = null; }
    }

    private void tick() {
        for (MapSession session : MapBuilderPlugin.getInstance().getActiveSessions()) {
            Player player = session.getBuilder();
            if (!player.isOnline()) continue;
            String bar = buildBar(player, session);
            ActionBarService.display(player, ActionBarChannel.GAME_STATUS, LegacyComponentSerializer.legacySection().deserialize(bar), 2500);
        }
    }

    private String buildBar(Player player, MapSession session) {
        BuilderSessionState state = session.getState();

        // ── Point Tool ──
        if (PointToolHandler.isHoldingTool(player)) {
            if (state.hasPendingRegionAnchor()) {
                MapPointDefinition t = state.getPendingRegionType();
                return "§6◈ §e" + t.displayName + " §7| §6Point 1 set — Right-Click Point 2  §8[Q=cancel]";
            }
            if (state.hasSelectedPoint()) {
                MapPointDefinition sel = state.getSelectedPoint();
                int count = session.countPoints(sel.exportKey);
                return "§a✔ §e" + sel.displayName
                        + " §7| §f" + count + " placed"
                        + "  §8[Right-Click=place  Shift+Right=change  Q=deselect]";
            }
            return "§7No point selected  §8[Right-Click=open menu]";
        }

        // ── Boundary Tool ──
        if (BoundaryToolHandler.isHoldingTool(player)) {
            boolean hasMin = session.getMinBoundary() != null;
            boolean hasMax = session.getMaxBoundary() != null;
            return "§bBoundary Tool  §7| L-Click=§fMin §7R-Click=§fMax"
                    + "  §8[" + (hasMin ? "§aMin✔" : "§cMin✗") + " §8| " + (hasMax ? "§aMax✔" : "§cMax✗") + "§8]";
        }

        // ── Display Tool ──
        if (DisplayToolHandler.isHoldingTool(player)) {
            String sel = state.getSelectedDisplayModel() != null
                    ? "§dModel: §f" + state.getSelectedDisplayModel()
                    : state.getSelectedDisplayMaterial() != null
                        ? "§dMaterial: §f" + formatMat(state.getSelectedDisplayMaterial())
                        : "§7Nothing selected";
            return "§dDisplay Tool  §7| " + sel + "  §8[Right-Click=place  Shift+Right=browse]";
        }

        // ── Eraser ──
        if (EraserToolHandler.isHoldingTool(player)) {
            return "§cEraser  §7| §fL-Click=Delete nearest  R-Click=Inspect";
        }

        // ── Unknown / other slot ──
        String gameType = session.getState().getTemplate().getDisplayName();
        return "§8MapBuilder §7| " + gameType + " §7— §e" + session.getMapName();
    }

    private String formatMat(Material mat) {
        if (mat == null) return "None";
        String name = mat.name().replace('_', ' ');
        StringBuilder sb = new StringBuilder();
        for (String word : name.split(" ")) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1).toLowerCase()).append(' ');
            }
        }
        return sb.toString().trim();
    }
}
