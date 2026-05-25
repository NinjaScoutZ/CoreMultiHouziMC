package com.houzicore.shared.core.lifecycle;

/**
 * Marks an object as owning visual or physical entities (Holograms, NPCs, DisplayModels).
 * Allows for robust auto-cleanup when the owner's lifecycle terminates.
 */
public interface LifecycleOwner {
    
    /**
     * @return A unique identifier for this lifecycle.
     */
    String getLifecycleId();
}
