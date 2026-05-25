package com.houzicore.shared.api.loadout;

import java.util.Optional;

public interface LoadoutItemRegistry {
    
    void register(LoadoutProfile profile, LoadoutItemFactory factory);
    
    Optional<LoadoutItemFactory> getFactory(LoadoutProfile profile);
}
