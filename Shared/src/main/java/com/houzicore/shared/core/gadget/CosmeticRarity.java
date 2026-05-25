package com.houzicore.shared.core.gadget;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum CosmeticRarity {
    COMMON(ChatColor.GRAY, ChatColor.GRAY + "" + ChatColor.BOLD + "✦ Common", Material.LIGHT_GRAY_STAINED_GLASS_PANE),
    RARE(ChatColor.BLUE, ChatColor.BLUE + "" + ChatColor.BOLD + "✦ Rare", Material.BLUE_STAINED_GLASS_PANE),
    EPIC(ChatColor.DARK_PURPLE, ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "✦ Epic", Material.PURPLE_STAINED_GLASS_PANE),
    LEGENDARY(ChatColor.GOLD, ChatColor.GOLD + "" + ChatColor.BOLD + "✦ Legends", Material.ORANGE_STAINED_GLASS_PANE),
    MYTHIC(ChatColor.LIGHT_PURPLE, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "✦ Mythic", Material.PINK_STAINED_GLASS_PANE);

    private final ChatColor color;
    private final String displayName;
    private final Material borderMaterial;

    CosmeticRarity(ChatColor color, String displayName, Material borderMaterial) {
        this.color = color;
        this.displayName = displayName;
        this.borderMaterial = borderMaterial;
    }

    public ChatColor getColor() {
        return color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getBorderMaterial() {
        return borderMaterial;
    }

    public String formatName(String name) {
        return color + "" + ChatColor.BOLD + name;
    }

    // ── Utility methods (Swofty-inspired) ────────────────────────

    /**
     * Get the next higher rarity tier. Returns itself if already at max.
     */
    public CosmeticRarity upgrade() {
        CosmeticRarity[] vals = values();
        return vals[Math.min(ordinal() + 1, vals.length - 1)];
    }

    /**
     * Get the next lower rarity tier. Returns itself if already at min.
     */
    public CosmeticRarity downgrade() {
        return ordinal() == 0 ? this : values()[ordinal() - 1];
    }

    /**
     * Check if this rarity is at least the given tier.
     */
    public boolean isAtLeast(CosmeticRarity other) {
        return ordinal() >= other.ordinal();
    }

    /**
     * Get the rarity display with bold formatting (e.g. for lore footers).
     */
    public String getBoldedDisplay() {
        return color + "" + ChatColor.BOLD + name().replace("_", " ");
    }

    /**
     * Get a loot drop weight for this rarity (higher rarity = lower weight).
     * Useful for treasure chest / reward randomization.
     */
    public double getDropWeight() {
        return switch (this) {
            case COMMON -> 50.0;
            case RARE -> 25.0;
            case EPIC -> 15.0;
            case LEGENDARY -> 8.0;
            case MYTHIC -> 2.0;
        };
    }

    /**
     * Get the coin multiplier bonus for this rarity tier.
     */
    public double getCoinMultiplier() {
        return switch (this) {
            case COMMON -> 1.0;
            case RARE -> 1.1;
            case EPIC -> 1.25;
            case LEGENDARY -> 1.5;
            case MYTHIC -> 2.0;
        };
    }
}
