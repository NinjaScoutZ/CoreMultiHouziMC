package com.houzicore.shared.api.feature;

import java.util.Objects;

public record FeaturePolicy(FeatureKey key, boolean enabled) {

    public FeaturePolicy {
        Objects.requireNonNull(key, "key");
    }
}
