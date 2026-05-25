package com.houzicore.mapbuilder.bootstrap;

import com.houzicore.shared.api.PlayerStateApplier;
import com.houzicore.shared.api.context.ContextPolicyRegistry;
import com.houzicore.shared.api.context.PlayerContextId;
import com.houzicore.shared.api.context.PlayerContextService;
import com.houzicore.shared.api.snapshot.PlayerSnapshotService;

/**
 * Central service holder for MapBuilder.
 * Parallel to ArcadeBootstrap / LobbyBootstrap — installs context policies and
 * exposes services to the rest of the plugin without static god-class coupling.
 *
 * The exitContextId is deliberately configurable so that MapBuilder can run
 * on any server topology without assuming the exit target is LOBBY_FREE.
 */
public class MapBuilderBootstrap {

    private static MapBuilderBootstrap instance;

    private final PlayerContextService contextService;
    private final PlayerStateApplier playerStateApplier;
    private final PlayerSnapshotService snapshotService;

    /**
     * Context to transition the player into when a MAP_EDIT session ends.
     * Defaults to LOBBY_FREE but must be overridden for standalone deployments.
     */
    private final PlayerContextId exitContextId;

    public MapBuilderBootstrap(
            PlayerContextService contextService,
            ContextPolicyRegistry policyRegistry,
            PlayerStateApplier playerStateApplier,
            PlayerSnapshotService snapshotService,
            PlayerContextId exitContextId) {

        this.contextService = contextService;
        this.playerStateApplier = playerStateApplier;
        this.snapshotService = snapshotService;
        this.exitContextId = exitContextId;

        MapBuilderContextInstaller.install(policyRegistry);

        instance = this;
    }

    public static MapBuilderBootstrap getInstance() {
        return instance;
    }

    public PlayerContextService getContextService() {
        return contextService;
    }

    public PlayerStateApplier getPlayerStateApplier() {
        return playerStateApplier;
    }

    public PlayerSnapshotService getSnapshotService() {
        return snapshotService;
    }

    public PlayerContextId getExitContextId() {
        return exitContextId;
    }
}
