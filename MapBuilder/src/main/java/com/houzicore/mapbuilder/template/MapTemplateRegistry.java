package com.houzicore.mapbuilder.template;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Central registry that maps GameType strings to MapTemplate instances.
 * Initialised once during MapBuilderPlugin#onEnable.
 * Falls back to GenericTemplate for unknown game types.
 */
public final class MapTemplateRegistry {

    private static MapTemplateRegistry instance;

    private final Map<String, MapTemplate> registry = new HashMap<>();

    private MapTemplateRegistry() {
        register(LobbyTemplate.create());
        register(HideAndSeekTemplate.create());
        register(PropRushTemplate.create(), "PropRush", "Prop Rush", "BlockHunt", "Block Hunt");
    }

    public static MapTemplateRegistry getInstance() {
        if (instance == null) instance = new MapTemplateRegistry();
        return instance;
    }

    public void register(MapTemplate template) {
        registry.put(template.getGameType().toLowerCase(), template);
    }

    public void register(MapTemplate template, String... aliases) {
        register(template);
        for (String alias : aliases) {
            if (alias == null || alias.isBlank()) continue;
            registry.put(alias.toLowerCase(), template);
        }
    }

    /**
     * Returns the template matching the given gameType (case-insensitive),
     * or a GenericTemplate fallback if none is registered.
     */
    public MapTemplate get(String gameType) {
        MapTemplate t = registry.get(gameType == null ? "" : gameType.toLowerCase());
        return t != null ? t : GenericTemplate.create(gameType != null ? gameType : "Unknown");
    }

    public Set<String> getGameTypes() {
        return Set.copyOf(registry.keySet());
    }
}
