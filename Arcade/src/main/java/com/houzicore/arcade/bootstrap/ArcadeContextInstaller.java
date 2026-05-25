package com.houzicore.arcade.bootstrap;

import com.houzicore.shared.api.context.ContextPolicy;
import com.houzicore.shared.api.context.ContextPolicyRegistry;
import com.houzicore.shared.api.context.PlayerContextId;
import com.houzicore.shared.api.feature.FeatureKey;
import com.houzicore.shared.api.loadout.SharedLoadoutProfiles;

public final class ArcadeContextInstaller {

    private ArcadeContextInstaller() { }

    public static void install(ContextPolicyRegistry registry) {
        
        // 1. ARCADE_LOBBY - Waiting for game to start, cosmetic active, PvP off
        registry.register(
            ContextPolicy.builder(PlayerContextId.ARCADE_LOBBY)
                .loadout(SharedLoadoutProfiles.ARCADE_LOBBY)
                .enableFeature(FeatureKey.COSMETIC_VISIBILITY)
                .enableFeature(FeatureKey.COSMETIC_EFFECTS)
                .enableFeature(FeatureKey.VISIBILITY) // Can see other players
                .build()
        );

        // 2. ARCADE_PREP - Spawned in, countdown running, frozen, kits not given yet
        registry.register(
            ContextPolicy.builder(PlayerContextId.ARCADE_PREP)
                .loadout(SharedLoadoutProfiles.EMPTY_LOADOUT)
                .enableFeature(FeatureKey.VISIBILITY)
                .build()
        );

        // 3. ARCADE_LIVE - Active fighting, loadout explicitly provided by Game class
        registry.register(
            ContextPolicy.builder(PlayerContextId.ARCADE_LIVE)
                .loadout(SharedLoadoutProfiles.EMPTY_LOADOUT) // Let game provide items
                .enableFeature(FeatureKey.ARENA_ITEM_USE)
                .enableFeature(FeatureKey.PVP)
                .enableFeature(FeatureKey.VISIBILITY)
                .build()
        );

        // 4. ARCADE_DEAD - Player died, frozen or respawning, invisible
        registry.register(
            ContextPolicy.builder(PlayerContextId.ARCADE_DEAD)
                .loadout(SharedLoadoutProfiles.EMPTY_LOADOUT)
                .build() // Visibility completely disabled as default
        );

        // 5. ARCADE_SPECTATOR - Flying, observing the game, invisible to live players
        registry.register(
            ContextPolicy.builder(PlayerContextId.ARCADE_SPECTATOR)
                .loadout(SharedLoadoutProfiles.ARCADE_SPECTATOR)
                .enableFeature(FeatureKey.LOBBY_ITEM_USE) // Use spectator compass etc
                .build() // Visibility disabled except maybe for other spectators
        );

        // 6. ARCADE_POSTGAME - Match ended, waiting for lobby handoff
        registry.register(
            ContextPolicy.builder(PlayerContextId.ARCADE_POSTGAME)
                .loadout(SharedLoadoutProfiles.ARCADE_POSTGAME)
                .enableFeature(FeatureKey.VISIBILITY)
                .build()
        );
    }
}
