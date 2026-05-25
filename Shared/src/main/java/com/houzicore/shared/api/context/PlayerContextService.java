package com.houzicore.shared.api.context;

import java.util.Optional;

import org.bukkit.entity.Player;

public interface PlayerContextService {

    Optional<PlayerContext> getCurrentContext(Player player);

    PlayerContextId getCurrentContextId(Player player);

    PlayerContext transition(Player player, PlayerContextId target, TransitionReason reason);

    boolean isInContext(Player player, PlayerContextId contextId);

    /**
     * Best-effort memory cleanup only.
     * Does NOT implicitly restore state (e.g. it will not inject missing items into an offline player's saved data).
     * Exists strictly to prevent memory leaks in the InMemory maps when a player disconnects.
     * @param playerId the UUID of the player who disconnected
     */
    void cleanup(java.util.UUID playerId);
}
