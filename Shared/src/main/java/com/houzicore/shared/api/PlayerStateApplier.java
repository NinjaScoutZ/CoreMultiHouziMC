package com.houzicore.shared.api;

import org.bukkit.entity.Player;

import com.houzicore.shared.api.context.PlayerContextId;

/**
 * The central contract for mutating player state (Inventory, Health, Location, Visibility, Flight).
 * Consolidates all state updates into a single orchestrator, reading rules from ContextPolicy and FeatureGate.
 */
public interface PlayerStateApplier {

    /**
     * Applies the appropriate state (loadout, effects, features) for the given context.
     *
     * @param player The player to apply state to.
     * @param contextId The context the player is entering.
     */
    void applyContextState(Player player, PlayerContextId contextId);

    /**
     * Refreshes the player's current state based on their active context.
     * Useful when a state mutation needs to be enforced (e.g. after respawn).
     *
     * @param player The player to refresh.
     */
    void refreshState(Player player);
    
    /**
     * Strips all buffs, effects, and items, returning the player to a neutral baseline.
     *
     * @param player The player to clean.
     */
    void cleanState(Player player);
}
