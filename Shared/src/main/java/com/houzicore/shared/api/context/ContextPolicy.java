package com.houzicore.shared.api.context;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import com.houzicore.shared.api.feature.FeatureKey;
import com.houzicore.shared.api.loadout.LoadoutProfile;

public record ContextPolicy(
        PlayerContextId contextId,
        LoadoutProfile loadoutProfile,
        Set<FeatureKey> enabledFeatures,
        boolean captureSnapshotOnEnter,
        boolean restoreSnapshotOnExit) {

    public ContextPolicy {
        Objects.requireNonNull(contextId, "contextId");
        Objects.requireNonNull(loadoutProfile, "loadoutProfile");
        Objects.requireNonNull(enabledFeatures, "enabledFeatures");
        enabledFeatures = enabledFeatures.isEmpty()
                ? Collections.emptySet()
                : Collections.unmodifiableSet(EnumSet.copyOf(enabledFeatures));
    }

    public boolean isEnabled(FeatureKey featureKey) {
        return enabledFeatures.contains(featureKey);
    }

    public static ContextPolicy.Builder builder(PlayerContextId contextId) {
        return new Builder(contextId);
    }

    public static class Builder {
        private final PlayerContextId contextId;
        private LoadoutProfile loadoutProfile;
        private final Set<FeatureKey> enabledFeatures = EnumSet.noneOf(FeatureKey.class);
        private boolean captureSnapshotOnEnter = false;
        private boolean restoreSnapshotOnExit = false;

        private Builder(PlayerContextId contextId) {
            this.contextId = Objects.requireNonNull(contextId, "contextId");
        }

        public Builder loadout(LoadoutProfile loadoutProfile) {
            this.loadoutProfile = Objects.requireNonNull(loadoutProfile, "loadoutProfile");
            return this;
        }

        public Builder enableFeature(FeatureKey feature) {
            this.enabledFeatures.add(Objects.requireNonNull(feature, "feature"));
            return this;
        }

        public Builder captureSnapshotOnEnter(boolean captureSnapshotOnEnter) {
            this.captureSnapshotOnEnter = captureSnapshotOnEnter;
            return this;
        }

        public Builder restoreSnapshotOnExit(boolean restoreSnapshotOnExit) {
            this.restoreSnapshotOnExit = restoreSnapshotOnExit;
            return this;
        }

        public ContextPolicy build() {
            return new ContextPolicy(
                    contextId,
                    loadoutProfile != null ? loadoutProfile : LoadoutProfile.of("default"),
                    enabledFeatures,
                    captureSnapshotOnEnter,
                    restoreSnapshotOnExit
            );
        }
    }
}
