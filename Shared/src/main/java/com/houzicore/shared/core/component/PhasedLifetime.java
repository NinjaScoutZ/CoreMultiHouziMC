package com.houzicore.shared.core.component;

/**
 * Represents a component that has a distinct active and inactive state.
 * Useful for listeners, task runners, and subsystems that must be safely enabled and disabled.
 */
public interface PhasedLifetime {
    
    /**
     * Activates this component.
     */
    void activate();

    /**
     * Deactivates this component and cleans up any allocated visual or memory resources.
     */
    void deactivate();

    /**
     * @return true if the component is currently active.
     */
    boolean isActive();
}
