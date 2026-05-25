package com.houzicore.mapbuilder.domain;

import org.bukkit.Material;

/**
 * Logical grouping for point types, used in PointPaletteGui to filter the
 * palette and in BuilderDashboardGui to label category buttons.
 */
public enum PointCategory {

    SPAWNS  ("§c§lGame Spawns",         "Spawn locations for teams and mobs",    Material.DIAMOND_SWORD),
    ZONES   ("§6§lZones & Regions",     "Two-point zones that define areas",      Material.MAP),
    NPCS    ("§e§lNPCs & Interactives", "NPC positions, treasures, holograms",   Material.VILLAGER_SPAWN_EGG),
    DISPLAYS("§d§lDecorations",         "Waterfall emitters and display markers", Material.PAINTING);

    public final String displayName;
    public final String description;
    public final Material icon;

    PointCategory(String displayName, String description, Material icon) {
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
    }
}
