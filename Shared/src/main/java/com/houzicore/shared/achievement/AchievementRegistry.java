package com.houzicore.shared.achievement;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Central registry for all achievement definitions.
 * Call register() at startup, then query by id or category.
 *
 * Ported pattern from: HypixelSkyBlock's static registration approach
 */
public final class AchievementRegistry {

    private static final Map<String, AchievementDefinition> BY_ID = new LinkedHashMap<>();

    private AchievementRegistry() {}

    public static void register(AchievementDefinition def) {
        BY_ID.put(def.getId(), def);
    }

    public static AchievementDefinition getById(String id) {
        return BY_ID.get(id);
    }

    public static List<AchievementDefinition> getByCategory(GameCategory category) {
        return BY_ID.values().stream()
                .filter(d -> d.getCategory() == category)
                .collect(Collectors.toList());
    }

    public static Collection<AchievementDefinition> getAll() {
        return Collections.unmodifiableCollection(BY_ID.values());
    }

    public static int getTotalPoints() {
        return BY_ID.values().stream().mapToInt(AchievementDefinition::getTotalPoints).sum();
    }
}
