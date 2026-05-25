package com.houzicore.shared.common.util;

/**
 * Registry of known acronyms that should NOT be title-cased.
 * Used by {@link UtilText#toTitleCase(String)} to preserve "TNT Run" instead of "Tnt Run".
 *
 * Ported from: net.swofty.commons.Acronym (HypixelSkyBlock)
 */
public enum AcronymRegistry {
    TNT, PVP, PVE, NPC, VIP, MVP, GUI, HP, XP, AFK, FPS, TPS,
    API, SQL, NMS, HUD, UHC, SG, CTF, KIT, ELO, GG, OP, RNG;

    /**
     * Checks if a word is a known acronym (case-insensitive).
     */
    public static boolean isAcronym(String s) {
        if (s == null || s.isEmpty()) return false;
        for (AcronymRegistry a : values()) {
            if (s.equalsIgnoreCase(a.name())) return true;
        }
        return false;
    }
}
