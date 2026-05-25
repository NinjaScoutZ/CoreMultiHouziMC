package com.houzicore.shared.core.cosmetic.collection;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public enum CosmeticCollection {

    ELEMENTAL("Elemental Master", ChatColor.AQUA, Material.HEART_OF_THE_SEA,
            new String[]{
                "Blizzard", // ParticleBlizzard
                "Fire Rings", // ParticleFireRings
                "Rain" // ParticleRain
            },
            "Unlock the 'Elemental Storm' particle trail!"),

    SHAPESHIFTER("Shapeshifter", ChatColor.DARK_PURPLE, Material.ENDER_EYE,
            new String[]{
                "Bat Morph",
                "Creeper Morph",
                "Enderman Morph"
            },
            "All morphs gain extended particle effects!"),

    ANIMAL_KINGDOM("Animal Kingdom", ChatColor.GREEN, Material.BAMBOO,
            new String[]{
                "Wolf",
                "Cat",
                "Fox",
                "Parrot",
                "Panda"
            },
            "Pets gain a subtle trail of hearts!"),

    ARSENAL("The Arsenal", ChatColor.RED, Material.IRON_SWORD,
            new String[]{
                "Dueling Sword",
                "Paintball Gun",
                "Flesh Hook"
            },
            "Gadget cooldowns are reduced globally!"),

    DJ_SET("DJ", ChatColor.YELLOW, Material.MUSIC_DISC_CHIRP,
            new String[]{
                "Cat Disc",
                "Blocks Disc",
                "Chirp Disc",
                "Far Disc",
                "Mall Disc"
            },
            "Access to the personal Radio Playlist!");

    private final String displayName;
    private final ChatColor color;
    private final Material icon;
    private final List<String> requiredItems;
    private final String bonusDescription;

    CosmeticCollection(String displayName, ChatColor color, Material icon, String[] requiredItems, String bonusDescription) {
        this.displayName = displayName;
        this.color = color;
        this.icon = icon;
        this.requiredItems = Arrays.asList(requiredItems);
        this.bonusDescription = bonusDescription;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ChatColor getColor() {
        return color;
    }

    public Material getIcon() {
        return icon;
    }

    public List<String> getRequiredItems() {
        return requiredItems;
    }

    public String getBonusDescription() {
        return bonusDescription;
    }
}
