package com.houzicore.shared.api.context;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record PlayerContext(
        UUID playerId,
        PlayerContextId contextId,
        TransitionReason reason,
        Instant enteredAt) {

    public PlayerContext {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(contextId, "contextId");
        Objects.requireNonNull(reason, "reason");
        Objects.requireNonNull(enteredAt, "enteredAt");
    }
}
