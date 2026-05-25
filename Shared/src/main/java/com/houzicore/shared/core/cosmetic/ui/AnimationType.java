package com.houzicore.shared.core.cosmetic.ui;

/**
 * Types of item name/lore animation used in cosmetic GUI pages.
 * Animated via BukkitRunnable ticking every 3 ticks in AnimatedMenuPage.
 */
public enum AnimationType {
    /** Each character cycles through rainbow colors (§c→§6→§e→§a→§b→§d) */
    RAINBOW_CYCLE,
    /** A "bright" band (§f§l) slides across the text character by character */
    SHIMMER,
    /** Name pulses between two colors (e.g. §6⟷§e) */
    GRADIENT_PULSE,
    /** Decorative symbols (✦✧★☆) rotate around the name */
    SPARKLE,
    /** Text types out one character at a time, then resets */
    TYPEWRITER,
}
