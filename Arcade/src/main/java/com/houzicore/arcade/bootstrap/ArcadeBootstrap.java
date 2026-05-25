package com.houzicore.arcade.bootstrap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.houzicore.arcade.Arcade;
import com.houzicore.shared.api.context.ContextPolicyRegistry;
import com.houzicore.shared.api.context.PlayerContextId;
import com.houzicore.shared.api.context.PlayerContextService;
import com.houzicore.shared.api.context.TransitionReason;
import com.houzicore.shared.api.feature.FeatureGate;
import com.houzicore.shared.api.loadout.LoadoutItemRegistry;
import com.houzicore.shared.api.loadout.LoadoutService;
import com.houzicore.shared.api.loadout.SharedLoadoutProfiles;
import com.houzicore.shared.api.snapshot.PlayerSnapshotService;
import com.houzicore.shared.core.feature.ContextAwareFeatureGate;
import com.houzicore.shared.core.context.InMemoryContextPolicyRegistry;
import com.houzicore.shared.core.context.InMemoryPlayerContextService;
import com.houzicore.shared.core.loadout.InMemoryLoadoutItemRegistry;
import com.houzicore.shared.core.loadout.InMemoryLoadoutService;
import com.houzicore.shared.core.snapshot.InMemoryPlayerSnapshotService;

public class ArcadeBootstrap implements Listener {

    private static ArcadeBootstrap instance;
    private static final String ROUND_RUNTIME_SNAPSHOT_ID = "arcade_round_runtime";

    private final ContextPolicyRegistry policyRegistry;
    private final PlayerContextService contextService;
    private final PlayerSnapshotService snapshotService;
    private final FeatureGate featureGate;
    private final LoadoutItemRegistry loadoutRegistry;
    private final LoadoutService loadoutService;
    private final ArcadePlayerStateApplier playerStateApplier;

    private ArcadeBootstrap(Arcade arcadePlugin) {
        this.policyRegistry = new InMemoryContextPolicyRegistry();
        this.contextService = new InMemoryPlayerContextService(PlayerContextId.ARCADE_LOBBY);
        this.snapshotService = new InMemoryPlayerSnapshotService();
        this.featureGate = new ContextAwareFeatureGate(contextService, policyRegistry);
        this.loadoutRegistry = new InMemoryLoadoutItemRegistry();
        this.loadoutService = new InMemoryLoadoutService(this.loadoutRegistry);
        this.playerStateApplier = new ArcadePlayerStateApplier(contextService, policyRegistry, loadoutService);

        ArcadeContextInstaller.install(policyRegistry);
        this.loadoutRegistry.register(SharedLoadoutProfiles.ARCADE_LOBBY, player ->
                this.playerStateApplier.buildArcadeLobbyItems(player));
        this.loadoutRegistry.register(SharedLoadoutProfiles.ARCADE_SPECTATOR, player ->
                this.playerStateApplier.buildArcadeSpectatorItems(player));
        this.loadoutRegistry.register(SharedLoadoutProfiles.ARCADE_POSTGAME, player ->
                this.playerStateApplier.buildArcadePostgameItems(player));
        this.loadoutRegistry.register(SharedLoadoutProfiles.EMPTY_LOADOUT, player -> java.util.Collections.emptyList());

        Bukkit.getPluginManager().registerEvents(this, arcadePlugin);
    }

    public static void init(Arcade arcadePlugin) {
        if (instance == null) {
            instance = new ArcadeBootstrap(arcadePlugin);
        }
    }

    public static ArcadeBootstrap getInstance() {
        return instance;
    }

    public ContextPolicyRegistry getPolicyRegistry() {
        return policyRegistry;
    }

    public PlayerContextService getContextService() {
        return contextService;
    }

    public PlayerSnapshotService getSnapshotService() {
        return snapshotService;
    }

    public FeatureGate getFeatureGate() {
        return featureGate;
    }

    public LoadoutService getLoadoutService() {
        return loadoutService;
    }

    public ArcadePlayerStateApplier getPlayerStateApplier() {
        return playerStateApplier;
    }

    /**
     * Canonical helper for Arcade runtime transitions.
     */
    public void transition(Player player, PlayerContextId contextId, TransitionReason reason) {
        contextService.transition(player, contextId, reason);
    }

    /**
     * Canonical helper for flows that change both logical context and physical player state.
     */
    public void transitionAndApply(Player player, PlayerContextId contextId, TransitionReason reason) {
        transition(player, contextId, reason);
        playerStateApplier.applyContextState(player, contextId);
    }

    /**
     * Re-apply whatever Arcade context the player already owns.
     */
    public void applyCurrentContextState(Player player) {
        playerStateApplier.refreshState(player);
    }

    /**
     * Explicitly apply a target Arcade context without mutating the stored context record.
     */
    public void applyContextState(Player player, PlayerContextId contextId) {
        playerStateApplier.applyContextState(player, contextId);
    }

    public void captureRoundSnapshot(Player player) {
        snapshotService.capture(player, ROUND_RUNTIME_SNAPSHOT_ID);
    }

    public boolean hasRoundSnapshot(Player player) {
        return snapshotService.hasSnapshot(player, ROUND_RUNTIME_SNAPSHOT_ID);
    }

    public void discardRoundSnapshot(Player player) {
        snapshotService.discard(player, ROUND_RUNTIME_SNAPSHOT_ID);
    }

    public void restoreRoundSnapshotToLobby(Player player, TransitionReason reason) {
        transition(player, PlayerContextId.ARCADE_LOBBY, reason);

        if (hasRoundSnapshot(player)) {
            playerStateApplier.cleanState(player);
            snapshotService.restore(player, ROUND_RUNTIME_SNAPSHOT_ID);
            playerStateApplier.applyRestoredArcadeLobbyState(player);
            return;
        }

        playerStateApplier.cleanState(player);
        playerStateApplier.applyContextState(player, PlayerContextId.ARCADE_LOBBY);
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Enforce implicit transition to default state (ARCADE_LOBBY)
        transitionAndApply(event.getPlayer(), PlayerContextId.ARCADE_LOBBY, TransitionReason.JOIN);
    }
}
