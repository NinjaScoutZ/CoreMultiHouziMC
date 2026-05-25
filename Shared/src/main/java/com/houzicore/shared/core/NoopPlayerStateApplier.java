package com.houzicore.shared.core;

import org.bukkit.entity.Player;

import com.houzicore.shared.api.PlayerStateApplier;
import com.houzicore.shared.api.context.PlayerContextId;

public class NoopPlayerStateApplier implements PlayerStateApplier {

    @Override
    public void applyContextState(Player player, PlayerContextId contextId) {
        // Skeleton logic for early wiring
    }

    @Override
    public void refreshState(Player player) {
        // Skeleton logic for early wiring
    }

    @Override
    public void cleanState(Player player) {
        // Skeleton logic for early wiring
    }
}
