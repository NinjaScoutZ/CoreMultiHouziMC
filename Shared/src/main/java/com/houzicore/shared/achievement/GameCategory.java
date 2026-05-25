package com.houzicore.shared.achievement;

import com.houzicore.shared.common.util.GUIMaterial;
import org.bukkit.Material;

/**
 * Categories for grouping achievements by game or system.
 * Each category has bilingual display names and a GUI icon.
 *
 * Ported from: net.swofty.type.generic.achievement.AchievementCategory
 * — Simplified from abstract class to enum for HouziCore's needs
 */
public enum GameCategory {

    GENERAL("General", "ทั่วไป", "general",
            new GUIMaterial(Material.NETHER_STAR)),
    SKYWARS("SkyWars", "สกายวอร์", "skywars",
            new GUIMaterial(Material.ENDER_EYE)),
    PROP_RUSH("Prop Rush", "พร็อพรัช", "proprush",
            new GUIMaterial(Material.CARVED_PUMPKIN)),
    HIDE_SEEK("Hide & Seek", "ซ่อนแอบ", "hideseek",
            new GUIMaterial(Material.OAK_DOOR)),
    DRAGON_RIDERS("Dragon Riders", "ดราก้อนไรเดอร์ส", "dragonriders",
            new GUIMaterial(Material.DRAGON_EGG));

    private final String displayNameEN;
    private final String displayNameTH;
    private final String configKey;
    private final GUIMaterial icon;

    GameCategory(String en, String th, String key, GUIMaterial icon) {
        this.displayNameEN = en;
        this.displayNameTH = th;
        this.configKey = key;
        this.icon = icon;
    }

    public String getDisplayName(boolean thai) {
        return thai ? displayNameTH : displayNameEN;
    }
    public String getConfigKey() { return configKey; }
    public GUIMaterial getIcon() { return icon; }

    /** Lookup by config key. Returns GENERAL if not found. */
    public static GameCategory fromConfigKey(String key) {
        for (GameCategory cat : values()) {
            if (cat.configKey.equalsIgnoreCase(key)) return cat;
        }
        return GENERAL;
    }
}
