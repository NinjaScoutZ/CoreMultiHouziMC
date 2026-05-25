package com.houzicore.mapbuilder;

import com.houzicore.mapbuilder.bootstrap.MapBuilderBootstrap;
import com.houzicore.mapbuilder.bootstrap.MapBuilderPlayerStateApplier;
import com.houzicore.mapbuilder.gui.PointPaletteGui;
import com.houzicore.mapbuilder.service.BuilderActionBarService;
import com.houzicore.mapbuilder.template.MapTemplateRegistry;
import com.houzicore.mapbuilder.tool.BoundaryToolHandler;
import com.houzicore.mapbuilder.tool.DisplayToolHandler;
import com.houzicore.mapbuilder.tool.EraserToolHandler;
import com.houzicore.mapbuilder.tool.FinishToolHandler;
import com.houzicore.mapbuilder.tool.PointToolHandler;
import com.houzicore.mapbuilder.tool.UndoRedoToolHandler;
import com.houzicore.shared.api.context.PlayerContextId;
import com.houzicore.shared.api.context.TransitionReason;
import com.houzicore.shared.core.context.InMemoryContextPolicyRegistry;
import com.houzicore.shared.core.context.InMemoryPlayerContextService;
import com.houzicore.shared.core.snapshot.InMemoryPlayerSnapshotService;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MapBuilderPlugin extends JavaPlugin implements Listener {

    private static MapBuilderPlugin instance;
    private com.houzicore.shared.updater.Updater updater;
    private com.houzicore.shared.core.displayentity.DisplayEntityManager displayEntityManager;

    // ── New Phase-2 tool handlers ──
    private PointToolHandler    pointToolHandler;
    private BoundaryToolHandler boundaryToolHandler;
    private DisplayToolHandler  displayToolHandler;
    private EraserToolHandler   eraserToolHandler;
    private UndoRedoToolHandler undoRedoToolHandler;
    private FinishToolHandler   finishToolHandler;
    private com.houzicore.mapbuilder.tool.DashboardToolHandler dashboardToolHandler;

    private final Map<UUID, MapSession> activeSessions = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("MapBuilder has been enabled.");

        // ── Core infrastructure ──
        this.updater = new com.houzicore.shared.updater.Updater(this);
        this.displayEntityManager = new com.houzicore.shared.core.displayentity.DisplayEntityManager(this);

        // ── Template registry (initialised automatically via singleton) ──
        MapTemplateRegistry.getInstance();

        // ── Visual layer ──
        new VisualManager();

        // ── NEW tool handlers (Phase 2) ──
        this.pointToolHandler    = new PointToolHandler();
        this.boundaryToolHandler = new BoundaryToolHandler();
        this.displayToolHandler  = new DisplayToolHandler();
        this.eraserToolHandler   = new EraserToolHandler();
        this.undoRedoToolHandler  = new UndoRedoToolHandler();
        this.finishToolHandler    = new FinishToolHandler();
        this.dashboardToolHandler = new com.houzicore.mapbuilder.tool.DashboardToolHandler();
        getServer().getPluginManager().registerEvents(pointToolHandler,    this);
        getServer().getPluginManager().registerEvents(boundaryToolHandler, this);
        getServer().getPluginManager().registerEvents(displayToolHandler,  this);
        getServer().getPluginManager().registerEvents(eraserToolHandler,   this);
        getServer().getPluginManager().registerEvents(undoRedoToolHandler, this);
        getServer().getPluginManager().registerEvents(finishToolHandler,   this);
        getServer().getPluginManager().registerEvents(dashboardToolHandler,this);

        // ── NEW GUI ──
        PointPaletteGui paletteGui = PointPaletteGui.getInstance();
        getServer().getPluginManager().registerEvents(paletteGui, this);
        com.houzicore.mapbuilder.gui.DashboardGui dashboardGui = com.houzicore.mapbuilder.gui.DashboardGui.getInstance();
        getServer().getPluginManager().registerEvents(dashboardGui, this);

        // ── ActionBar service ──
        BuilderActionBarService.getInstance().start(this);

        // ── Legacy GUI managers kept alive until Phase 3 replaces them ──
        new BlockDisplayGui();
        new ModelEditorGui();

        // --- Context Runtime Bootstrap ---
        InMemoryContextPolicyRegistry policyRegistry = new InMemoryContextPolicyRegistry();
        InMemoryPlayerContextService contextService  = new InMemoryPlayerContextService(PlayerContextId.LOBBY_FREE);
        InMemoryPlayerSnapshotService snapshotService= new InMemoryPlayerSnapshotService();
        MapBuilderPlayerStateApplier stateApplier    = new MapBuilderPlayerStateApplier(contextService);

        new MapBuilderBootstrap(
                contextService, policyRegistry, stateApplier, snapshotService,
                PlayerContextId.LOBBY_FREE
        );

        // Register commands and events
        MapBuilderCommand mapBuilderCommand = new MapBuilderCommand();
        getCommand("mapbuilder").setExecutor(mapBuilderCommand);
        getCommand("mapbuilder").setTabCompleter(mapBuilderCommand);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        BuilderActionBarService.getInstance().stop();
        if (VisualManager.getInstance() != null) VisualManager.getInstance().cleanup();
        getLogger().info("MapBuilder has been disabled.");
    }

    // -------------------------------------------------------------------------
    // Session Lifecycle
    // -------------------------------------------------------------------------

    public void startSession(Player player, String gameType, String mapName) {
        startSession(player, gameType, mapName, com.houzicore.mapbuilder.session.EditMode.SANDBOX);
    }

    public void startSession(Player player, String gameType, String mapName, com.houzicore.mapbuilder.session.EditMode mode) {
        startSession(player, gameType, mapName, player.getWorld(), mode);
    }

    public void startSession(Player player, String gameType, String mapName, org.bukkit.World world, com.houzicore.mapbuilder.session.EditMode mode) {
        if (activeSessions.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You already have an active mapping session. Use /mb cancel or /mb export first.");
            return;
        }

        MapBuilderBootstrap bootstrap = MapBuilderBootstrap.getInstance();

        // 1. Capture current inventory before we clean it — snapshot owns restoration on exit
        String snapshotId = snapshotId(player);
        bootstrap.getSnapshotService().capture(player, snapshotId);

        // 2. Register session
        if (VisualManager.getInstance() != null) {
            VisualManager.getInstance().clearVisuals(player.getUniqueId());
        }
        MapSession session = new MapSession(player, gameType, mapName, player.getName(), world, mode);
        activeSessions.put(player.getUniqueId(), session);

        // 3. Wipe session-editor slate (cleanState uses UtilInv.Clear — safe, snapshot is already captured)
        bootstrap.getPlayerStateApplier().cleanState(player);

        // 4. Transition context and apply MAP_EDIT state (gives wands, creative, flight)
        bootstrap.getContextService().transition(player, PlayerContextId.MAP_EDIT, TransitionReason.MAP_EDIT_START);
        bootstrap.getPlayerStateApplier().applyContextState(player, PlayerContextId.MAP_EDIT);

        // 5. Removed redundant giveWands() call — MapBuilderPlayerStateApplier single-source-of-truth handles it.

        player.sendMessage(ChatColor.GREEN + "§lMapBuilder §r§7— §e" + gameType + " §7» §f" + mapName);
        player.sendMessage(ChatColor.AQUA + "Mode: " + ChatColor.YELLOW + mode.name());
        player.sendMessage(ChatColor.GRAY + "Slot 1=Point  2=Boundary  3=Display  4=Eraser  7=Undo  8=Finish");
    }

    /** Convenience overload — no force flag (uses export gate). */
    public void endSession(Player player, boolean export) {
        endSession(player, export, false);
    }

    /**
     * End the session, optionally exporting.
     * @param forceExport if true, skip the validation gate and export anyway.
     */
    public void endSession(Player player, boolean export, boolean forceExport) {
        if (export) {
            // Run gate BEFORE removing from map so we can bail without losing session
            MapSession checkSession = activeSessions.get(player.getUniqueId());
            if (checkSession != null && !forceExport) {
                com.houzicore.mapbuilder.service.ValidationReport report = com.houzicore.mapbuilder.service.ValidationService.validate(checkSession);
                
                if (report.hasWarnings()) {
                    player.sendMessage(ChatColor.YELLOW + "⚠ Warnings during validation:");
                    report.getWarnings().forEach(w -> player.sendMessage(ChatColor.GRAY + " - " + w));
                }
                
                if (report.hasErrors()) {
                    player.sendMessage(ChatColor.RED + "§lExport BLOCKED §r§7— fix these issues first:");
                    report.getErrors().forEach(e -> player.sendMessage(ChatColor.RED + " ✗ " + e));
                    player.sendMessage(ChatColor.YELLOW + "Shift+Right-Click §eFinish§7 or use §e/mb export --force §7to override.");
                    return; // session stays active
                }
            }
        }

        MapSession session = activeSessions.remove(player.getUniqueId());
        if (session == null) {
            player.sendMessage(ChatColor.RED + "You don't have an active mapping session.");
            return;
        }

        // 1. Export before restoring state so the session data is still intact
        if (export) {
            player.sendMessage(ChatColor.GREEN + "Exporting map " + session.getMapName() + "...");
            WorldConfigExporter.export(session);
        } else {
            player.sendMessage(ChatColor.YELLOW + "Cancelled mapping session for " + session.getMapName());
        }

        MapBuilderBootstrap bootstrap = MapBuilderBootstrap.getInstance();
        String snapshotId = snapshotId(player);

        // 2. Restore original inventory (snapshot restore) — before any state application
        bootstrap.getSnapshotService().restore(player, snapshotId);
        bootstrap.getSnapshotService().discard(player, snapshotId);

        // 3. Transition context then apply the exit state — no hardcoded LOBBY_FREE assumption
        PlayerContextId exitContext = bootstrap.getExitContextId();
        bootstrap.getContextService().transition(player, exitContext, TransitionReason.MAP_EDIT_END);
        bootstrap.getPlayerStateApplier().applyContextState(player, exitContext);

        // 4. Cleanup visual layer
        session.clearSession();
        if (VisualManager.getInstance() != null) {
            VisualManager.getInstance().clearVisuals(player.getUniqueId());
        }
    }

    // -------------------------------------------------------------------------
    // PlayerQuitEvent — prevent snapshot memory leak
    // -------------------------------------------------------------------------

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        activeSessions.remove(uuid); // remove any dangling session reference

        if (VisualManager.getInstance() != null) {
            VisualManager.getInstance().clearVisuals(uuid);
        }

        MapBuilderBootstrap bootstrap = MapBuilderBootstrap.getInstance();
        if (bootstrap != null) {
            bootstrap.getSnapshotService().cleanup(uuid);
            bootstrap.getContextService().cleanup(uuid); // prevent stale context in InMemory map
        }
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public static MapBuilderPlugin getInstance() { return instance; }

    public com.houzicore.shared.core.displayentity.DisplayEntityManager getDisplayEntityManager() {
        return displayEntityManager;
    }

    public MapSession getSession(Player player) {
        return activeSessions.get(player.getUniqueId());
    }

    public java.util.Collection<MapSession> getActiveSessions() {
        return activeSessions.values();
    }

    // -------------------------------------------------------------------------

    private static String snapshotId(Player player) {
        return "map_edit_" + player.getUniqueId();
    }
}
