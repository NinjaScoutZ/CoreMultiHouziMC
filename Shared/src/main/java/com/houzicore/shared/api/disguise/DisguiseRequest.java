package com.houzicore.shared.api.disguise;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record DisguiseRequest(
        UUID subjectId,
        DisguiseArchetype archetype,
        String variantKey,
        boolean selfVisible,
        boolean hideArmor,
        boolean hideHeldItem,
        String customName,
        boolean customNameVisible,
        Map<String, String> attributes) {

    public DisguiseRequest(
            UUID subjectId,
            DisguiseArchetype archetype,
            String variantKey,
            boolean selfVisible,
            boolean hideArmor,
            boolean hideHeldItem) {
        this(subjectId, archetype, variantKey, selfVisible, hideArmor, hideHeldItem, null, false);
    }

    public DisguiseRequest(
            UUID subjectId,
            DisguiseArchetype archetype,
            String variantKey,
            boolean selfVisible,
            boolean hideArmor,
            boolean hideHeldItem,
            String customName,
            boolean customNameVisible) {
        this(subjectId, archetype, variantKey, selfVisible, hideArmor, hideHeldItem, customName, customNameVisible, Map.of());
    }

    public DisguiseRequest {
        Objects.requireNonNull(subjectId, "subjectId");
        Objects.requireNonNull(archetype, "archetype");
        Objects.requireNonNull(variantKey, "variantKey");
        attributes = Map.copyOf(attributes == null ? Map.of() : attributes);
    }

    public DisguiseRequest withAttribute(String key, String value) {
        Map<String, String> nextAttributes = new HashMap<>(attributes);
        nextAttributes.put(key, value);
        return new DisguiseRequest(
                subjectId,
                archetype,
                variantKey,
                selfVisible,
                hideArmor,
                hideHeldItem,
                customName,
                customNameVisible,
                nextAttributes);
    }
}
