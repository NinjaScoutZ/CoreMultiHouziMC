package com.houzicore.mapbuilder.bootstrap;

import com.houzicore.shared.api.context.ContextPolicy;
import com.houzicore.shared.api.context.ContextPolicyRegistry;
import com.houzicore.shared.api.context.PlayerContextId;
import com.houzicore.shared.api.feature.FeatureKey;
import com.houzicore.shared.api.loadout.SharedLoadoutProfiles;

public class MapBuilderContextInstaller {

    public static void install(ContextPolicyRegistry registry) {
        // MAP_EDIT — full editor tools: flight, block interaction, wand access
        registry.register(ContextPolicy.builder(PlayerContextId.MAP_EDIT)
                .enableFeature(FeatureKey.MAP_EDIT_TOOLS)
                .enableFeature(FeatureKey.FLIGHT)
                .enableFeature(FeatureKey.BLOCK_BREAK)
                .enableFeature(FeatureKey.BLOCK_PLACE)
                .loadout(SharedLoadoutProfiles.MAP_EDITOR)
                .build());

        // MAP_PREVIEW — read-only fly-through, no editing capability
        registry.register(ContextPolicy.builder(PlayerContextId.MAP_PREVIEW)
                .enableFeature(FeatureKey.FLIGHT)
                .loadout(SharedLoadoutProfiles.EMPTY_LOADOUT)
                .build());
    }
}
