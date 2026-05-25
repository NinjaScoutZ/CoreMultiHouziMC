package com.houzicore.lobby.hub.bootstrap;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.houzicore.lobby.hub.Hub;
import com.houzicore.shared.api.context.ContextPolicyRegistry;
import com.houzicore.shared.api.context.PlayerContextId;
import com.houzicore.shared.api.context.PlayerContextService;
import com.houzicore.shared.api.context.TransitionReason;
import com.houzicore.shared.api.feature.FeatureGate;
import com.houzicore.shared.api.snapshot.PlayerSnapshotService;
import com.houzicore.shared.api.loadout.LoadoutService;
import com.houzicore.shared.api.loadout.LoadoutItemRegistry;
import com.houzicore.shared.api.loadout.SharedLoadoutProfiles;
import com.houzicore.shared.core.loadout.InMemoryLoadoutItemRegistry;
import com.houzicore.shared.core.loadout.InMemoryLoadoutService;
import com.houzicore.shared.core.context.InMemoryContextPolicyRegistry;
import com.houzicore.shared.core.context.InMemoryPlayerContextService;
import com.houzicore.shared.core.snapshot.InMemoryPlayerSnapshotService;
import com.houzicore.shared.core.feature.ContextAwareFeatureGate;

public class LobbyBootstrap implements Listener {

    private static LobbyBootstrap instance;

    private final ContextPolicyRegistry policyRegistry;
    private final PlayerContextService contextService;
    private final PlayerSnapshotService snapshotService;
    private final FeatureGate featureGate;
    private final LobbyPlayerStateApplier playerStateApplier;
    private final LoadoutItemRegistry loadoutRegistry;
    private final LoadoutService loadoutService;

    private LobbyBootstrap(Hub hub) {
        this.policyRegistry = new InMemoryContextPolicyRegistry();
        this.contextService = new InMemoryPlayerContextService(PlayerContextId.LOBBY_FREE);
        this.snapshotService = new InMemoryPlayerSnapshotService();
        this.featureGate = new ContextAwareFeatureGate(contextService, policyRegistry);
        this.loadoutRegistry = new InMemoryLoadoutItemRegistry();
        this.loadoutService = new InMemoryLoadoutService(this.loadoutRegistry);
        this.playerStateApplier = new LobbyPlayerStateApplier(contextService, policyRegistry, loadoutService);

        LobbyContextInstaller.install(policyRegistry);
        
        // Register LOBBY_MAIN items into factory
        this.loadoutRegistry.register(SharedLoadoutProfiles.LOBBY_MAIN, player -> 
            this.playerStateApplier.buildLobbyMainItems(player)
        );

        // Register LOBBY_ARENA_DUEL items into factory
        this.loadoutRegistry.register(SharedLoadoutProfiles.LOBBY_ARENA_DUEL, player -> 
            this.playerStateApplier.buildArenaDuelItems(player)
        );

        this.loadoutRegistry.register(SharedLoadoutProfiles.LOBBY_ACTIVITY, player ->
            java.util.Collections.emptyList()
        );
        this.loadoutRegistry.register(SharedLoadoutProfiles.LOBBY_FISHING, player ->
            this.playerStateApplier.buildFishingItems(player)
        );
        this.loadoutRegistry.register(SharedLoadoutProfiles.LOBBY_FARM, player ->
            this.playerStateApplier.buildFarmItems(player)
        );
        this.loadoutRegistry.register(SharedLoadoutProfiles.LOBBY_PARKOUR, player ->
            java.util.Collections.emptyList()
        );

        // Register EMPTY and SOCIAL loadouts
        this.loadoutRegistry.register(SharedLoadoutProfiles.EMPTY_LOADOUT, player -> java.util.Collections.emptyList());
        this.loadoutRegistry.register(SharedLoadoutProfiles.LOBBY_SOCIAL, player -> java.util.Collections.emptyList());

        // Configure CosmeticManager slots after initialization is complete
        Bukkit.getScheduler().runTaskLater(hub, () -> {
            try {
                com.houzicore.shared.core.cosmetic.CosmeticManager cosmeticManager = 
                    com.houzicore.shared.core.plugin.PluginRegistry.require(com.houzicore.shared.core.cosmetic.CosmeticManager.class);
                cosmeticManager.setInterfaceSlot(2);
                cosmeticManager.getGadgetManager().setActiveItemSlot(3);
            } catch (Exception e) {
                hub.getLogger().warning("Failed to configure CosmeticManager: " + e.getMessage());
            }
        }, 1L);

        Bukkit.getPluginManager().registerEvents(this, hub);
    }

    public static void init(Hub hub) {
        if (instance == null) {
            instance = new LobbyBootstrap(hub);
        }
    }

    public static LobbyBootstrap getInstance() {
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

    public LobbyPlayerStateApplier getPlayerStateApplier() {
        return playerStateApplier;
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Enforce implicit transition to default state (LOBBY_FREE)
        contextService.transition(event.getPlayer(), PlayerContextId.LOBBY_FREE, TransitionReason.JOIN);
        playerStateApplier.applyContextState(event.getPlayer(), PlayerContextId.LOBBY_FREE);
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Best-effort memory cleanup
        contextService.cleanup(event.getPlayer().getUniqueId());
        snapshotService.cleanup(event.getPlayer().getUniqueId());
    }
}
