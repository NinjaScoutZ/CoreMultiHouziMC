package com.houzicore.shared.core.loadout;

import org.bukkit.entity.Player;

import com.houzicore.shared.api.loadout.LoadoutProfile;
import com.houzicore.shared.api.loadout.LoadoutService;

public class NoopLoadoutService implements LoadoutService {

    @Override
    public void apply(Player player, LoadoutProfile profile) {
        // Skeleton implementation for early wiring and migration.
    }

    @Override
    public void clear(Player player) {
        // Skeleton implementation for early wiring and migration.
    }
}
