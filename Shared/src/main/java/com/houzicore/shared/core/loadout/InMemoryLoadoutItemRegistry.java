package com.houzicore.shared.core.loadout;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.houzicore.shared.api.loadout.LoadoutItemFactory;
import com.houzicore.shared.api.loadout.LoadoutItemRegistry;
import com.houzicore.shared.api.loadout.LoadoutProfile;

public class InMemoryLoadoutItemRegistry implements LoadoutItemRegistry {
    
    private final Map<LoadoutProfile, LoadoutItemFactory> registry = new HashMap<>();

    @Override
    public void register(LoadoutProfile profile, LoadoutItemFactory factory) {
        registry.put(profile, factory);
    }

    @Override
    public Optional<LoadoutItemFactory> getFactory(LoadoutProfile profile) {
        return Optional.ofNullable(registry.get(profile));
    }
}
