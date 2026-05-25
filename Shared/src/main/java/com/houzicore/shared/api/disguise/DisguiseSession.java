package com.houzicore.shared.api.disguise;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record DisguiseSession(
        UUID subjectId,
        DisguiseRequest request,
        DisguiseBackend backend,
        Instant appliedAt) {

    public DisguiseSession {
        Objects.requireNonNull(subjectId, "subjectId");
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(backend, "backend");
        Objects.requireNonNull(appliedAt, "appliedAt");
    }
}
