package com.houzicore.lobby.hub.bootstrap;

import com.houzicore.shared.api.context.ContextPolicy;
import com.houzicore.shared.api.context.ContextPolicyRegistry;
import com.houzicore.shared.api.context.PlayerContextId;
import com.houzicore.shared.api.feature.FeatureKey;
import com.houzicore.shared.api.loadout.SharedLoadoutProfiles;

public final class LobbyContextInstaller {

    private LobbyContextInstaller() { }

    public static void install(ContextPolicyRegistry registry) {
        
        // 1. LOBBY_FREE - The default open lobby
        registry.register(
            ContextPolicy.builder(PlayerContextId.LOBBY_FREE)
                .loadout(SharedLoadoutProfiles.LOBBY_MAIN)
                .enableFeature(FeatureKey.DOUBLE_JUMP)
                .enableFeature(FeatureKey.JUMP_PAD)
                .enableFeature(FeatureKey.GADGET)
                .enableFeature(FeatureKey.PET)
                .enableFeature(FeatureKey.MOUNT)
                .enableFeature(FeatureKey.COSMETIC_VISIBILITY)
                .enableFeature(FeatureKey.COSMETIC_EFFECTS)
                .enableFeature(FeatureKey.LOBBY_ITEM_USE)
                .enableFeature(FeatureKey.VISIBILITY) // Can see other players
                .build()
        );

        // 2. LOBBY_SOCIAL_UI - When inside profile or preferences menus
        registry.register(
            ContextPolicy.builder(PlayerContextId.LOBBY_SOCIAL_UI)
                .loadout(SharedLoadoutProfiles.LOBBY_SOCIAL)
                .enableFeature(FeatureKey.COSMETIC_VISIBILITY)
                .enableFeature(FeatureKey.COSMETIC_EFFECTS)
                .build()
        );

        // 3. LOBBY_ARENA_PREP - Walking into the Arena waiting for match
        registry.register(
            ContextPolicy.builder(PlayerContextId.LOBBY_ARENA_PREP)
                .loadout(SharedLoadoutProfiles.EMPTY_LOADOUT)
                // NO Double Jump, NO Cosmetics, NO Gadgets
                .enableFeature(FeatureKey.VISIBILITY)
                .captureSnapshotOnEnter(true) // Takes snapshot before removing items
                .build()
        );

        // 4. LOBBY_ARENA_LIVE - Active fighting
        registry.register(
            ContextPolicy.builder(PlayerContextId.LOBBY_ARENA_LIVE)
                .loadout(SharedLoadoutProfiles.LOBBY_ARENA_DUEL)
                .enableFeature(FeatureKey.ARENA_ITEM_USE)
                .enableFeature(FeatureKey.PVP)
                .enableFeature(FeatureKey.VISIBILITY)
                .build()
        );

        // 5. LOBBY_ACTIVITY - Farm/Fishing/Parkour (placeholder for future migration)
        registry.register(
            ContextPolicy.builder(PlayerContextId.LOBBY_ACTIVITY)
                .loadout(SharedLoadoutProfiles.LOBBY_ACTIVITY)
                .enableFeature(FeatureKey.LOBBY_ITEM_USE)
                .enableFeature(FeatureKey.VISIBILITY)
                .build()
        );

        registry.register(
            ContextPolicy.builder(PlayerContextId.LOBBY_FISHING)
                .loadout(SharedLoadoutProfiles.LOBBY_FISHING)
                .enableFeature(FeatureKey.LOBBY_ITEM_USE)
                .enableFeature(FeatureKey.VISIBILITY)
                .captureSnapshotOnEnter(true)
                .build()
        );

        registry.register(
            ContextPolicy.builder(PlayerContextId.LOBBY_FARM)
                .loadout(SharedLoadoutProfiles.LOBBY_FARM)
                .enableFeature(FeatureKey.LOBBY_ITEM_USE)
                .enableFeature(FeatureKey.VISIBILITY)
                .captureSnapshotOnEnter(true)
                .build()
        );

        registry.register(
            ContextPolicy.builder(PlayerContextId.LOBBY_PARKOUR)
                .loadout(SharedLoadoutProfiles.LOBBY_PARKOUR)
                .enableFeature(FeatureKey.VISIBILITY)
                .captureSnapshotOnEnter(true)
                .build()
        );
    }
}
