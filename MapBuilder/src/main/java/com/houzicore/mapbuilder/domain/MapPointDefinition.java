package com.houzicore.mapbuilder.domain;

import org.bukkit.Material;

/**
 * Canonical catalog of every point type recognised by MapBuilder.
 *
 * Each entry encodes:
 *   exportKey   – the string written to WorldConfig.dat (backward-compat)
 *   displayName – human-readable label shown in GUIs / ActionBar
 *   category    – grouping used in PointPaletteGui
 *   kind        – placement behaviour (SINGLE/MULTI/PAIR_REGION/DIRECTIONAL)
 *   icon        – Material used in palette
 *   minCount    – minimum placements required (0 = optional)
 *   maxCount    – -1 = unlimited
 */
public enum MapPointDefinition {

    // ── SPAWNS ────────────────────────────────────────────────────────────────
    HIDER_SPAWN  ("TEAM_NAME:Blue",        "Hider Spawn",    PointCategory.SPAWNS,   PlacementKind.DIRECTIONAL, Material.LIGHT_BLUE_DYE,  2, -1),
    HUNTER_SPAWN ("TEAM_NAME:Red",         "Hunter Spawn",   PointCategory.SPAWNS,   PlacementKind.DIRECTIONAL, Material.RED_DYE,          1, -1),
    LOBBY_SPAWN  ("DATA_NAME:LOBBY_SPAWN", "Lobby Spawn",    PointCategory.SPAWNS,   PlacementKind.DIRECTIONAL, Material.BEACON,           1,  1),
    FENCE_WALL   ("DATA_NAME:BLACK",       "Fence Wall",     PointCategory.SPAWNS,   PlacementKind.MULTI,       Material.OAK_FENCE,        0, -1),
    PIG_SPAWN    ("DATA_NAME:PINK",        "Pig Spawn",      PointCategory.SPAWNS,   PlacementKind.MULTI,       Material.PIG_SPAWN_EGG,    0, -1),
    SHEEP_SPAWN  ("DATA_NAME:WHITE",       "Sheep Spawn",    PointCategory.SPAWNS,   PlacementKind.MULTI,       Material.SHEEP_SPAWN_EGG,  0, -1),
    CHICKEN_SPAWN("DATA_NAME:YELLOW",      "Chicken Spawn",  PointCategory.SPAWNS,   PlacementKind.MULTI,       Material.CHICKEN_SPAWN_EGG,0, -1),
    COW_SPAWN    ("DATA_NAME:BROWN",       "Cow Spawn",      PointCategory.SPAWNS,   PlacementKind.MULTI,       Material.COW_SPAWN_EGG,    0, -1),
    KIT_NPC      ("DATA_NAME:RED",         "Hunter Kit NPC", PointCategory.SPAWNS,   PlacementKind.SINGLE,      Material.DIAMOND_SWORD,    0,  1),
    CUSTOM_DATA  ("CUSTOM_NAME:Custom",    "Custom Data",    PointCategory.SPAWNS,   PlacementKind.MULTI,       Material.PAPER,            0, -1),

    // ── ZONES ────────────────────────────────────────────────────────────────
    ARENA_CENTER ("DATA_NAME:ZONE_ARENA",  "Arena Center",   PointCategory.ZONES,    PlacementKind.SINGLE,      Material.IRON_SWORD,       0,  1),
    ARENA_SPAWN_A("DATA_NAME:ARENA_SPAWN_A","Arena Spawn A", PointCategory.ZONES,    PlacementKind.SINGLE,      Material.BLUE_DYE,         0,  1),
    ARENA_SPAWN_B("DATA_NAME:ARENA_SPAWN_B","Arena Spawn B", PointCategory.ZONES,    PlacementKind.SINGLE,      Material.RED_DYE,          0,  1),
    FISHING_ZONE ("DATA_NAME:ZONE_FISHING","Fishing Zone",   PointCategory.ZONES,    PlacementKind.MULTI, Material.FISHING_ROD,      0,  -1),
    PARKOUR_ZONE ("DATA_NAME:ZONE_PARKOUR","Parkour Zone",   PointCategory.ZONES,    PlacementKind.PAIR_REGION, Material.GOLDEN_BOOTS,     0,  2),
    PARKOUR_CP   ("DATA_NAME:PARKOUR_CP",  "Parkour Checkpoint",PointCategory.ZONES,  PlacementKind.MULTI,       Material.LIGHT_WEIGHTED_PRESSURE_PLATE, 0, -1),
    FARM_ZONE    ("DATA_NAME:ZONE_FARM",   "Farm Zone",      PointCategory.ZONES,    PlacementKind.PAIR_REGION, Material.WHEAT,            0,  2),
    TERMINAL     ("CUSTOM_NAME:TERMINAL",  "Terminal Point", PointCategory.ZONES,    PlacementKind.MULTI,       Material.BEACON,           0, -1),
    DANGER_ZONE  ("DATA_NAME:LIME",        "Danger Zone",    PointCategory.ZONES,    PlacementKind.MULTI,       Material.LIME_DYE,         0, -1),

    // ── NPCS ─────────────────────────────────────────────────────────────────
    NPC_FISHING  ("DATA_NAME:NPC_FISHING", "NPC: Fishing",   PointCategory.NPCS,     PlacementKind.SINGLE,      Material.VILLAGER_SPAWN_EGG,0, 1),
    NPC_PARKOUR  ("DATA_NAME:NPC_PARKOUR", "NPC: Parkour",   PointCategory.NPCS,     PlacementKind.SINGLE,      Material.VILLAGER_SPAWN_EGG,0, 1),
    NPC_ARENA    ("DATA_NAME:NPC_ARENA",   "NPC: Arena",     PointCategory.NPCS,     PlacementKind.SINGLE,      Material.VILLAGER_SPAWN_EGG,0, 1),
    NPC_FARM     ("DATA_NAME:NPC_FARM",    "NPC: Farm",      PointCategory.NPCS,     PlacementKind.SINGLE,      Material.VILLAGER_SPAWN_EGG,0, 1),
    TREASURE     ("DATA_NAME:TREASURE",    "Treasure Chest", PointCategory.NPCS,     PlacementKind.MULTI,       Material.CHEST,            0, -1),
    HOLO_LEADER  ("DATA_NAME:HOLO_LEADER", "Hologram: LB",   PointCategory.NPCS,     PlacementKind.MULTI,       Material.ARMOR_STAND,      0, -1),
    HOLO_CUSTOM  ("DATA_NAME:HOLO_CUSTOM", "Hologram: Text", PointCategory.NPCS,     PlacementKind.MULTI,       Material.ARMOR_STAND,      0, -1),

    // ── DISPLAYS ─────────────────────────────────────────────────────────────
    WATERFALL_EMITTER("DATA_NAME:WATERFALL_EMITTER","Waterfall Emitter",PointCategory.DISPLAYS,PlacementKind.MULTI,Material.WATER_BUCKET,0,-1);

    // ─────────────────────────────────────────────────────────────────────────

    /** The key written to WorldConfig.dat — must match ParseData exactly. */
    public final String exportKey;
    public final String displayName;
    public final PointCategory category;
    public final PlacementKind kind;
    public final Material icon;
    /** Minimum placements required for validation (0 = optional). */
    public final int minCount;
    /** Maximum placements allowed (-1 = unlimited). */
    public final int maxCount;

    MapPointDefinition(String exportKey, String displayName, PointCategory category,
                       PlacementKind kind, Material icon, int minCount, int maxCount) {
        this.exportKey   = exportKey;
        this.displayName = displayName;
        this.category    = category;
        this.kind        = kind;
        this.icon        = icon;
        this.minCount    = minCount;
        this.maxCount    = maxCount;
    }

    /** Resolve a raw exportKey string back to its definition, or null if unknown. */
    public static MapPointDefinition fromExportKey(String key) {
        if (key == null) return null;
        for (MapPointDefinition def : values()) {
            if (def.exportKey.equals(key)) return def;
        }
        return null;
    }

    /** All definitions that belong to a given category. */
    public static java.util.List<MapPointDefinition> byCategory(PointCategory cat) {
        java.util.List<MapPointDefinition> list = new java.util.ArrayList<>();
        for (MapPointDefinition def : values()) {
            if (def.category == cat) list.add(def);
        }
        return list;
    }
}
