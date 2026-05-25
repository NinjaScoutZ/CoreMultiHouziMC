package com.houzicore.shared.core.pet.ability;

import org.bukkit.entity.Player;
import java.util.List;

/**
 * Represents a gameplay-affecting ability that a pet can provide to its owner.
 * Inspired by Swofty's HypixelSkyBlock PetAbility pattern.
 */
public interface PetAbility {
    /**
     * Get the English display name of this ability.
     */
    String getName();

    /**
     * Get the Thai localized display name of this ability.
     */
    String getNameTh();

    /**
     * Get the English description lines for this ability (player-specific).
     */
    List<String> getDescription(Player player);

    /**
     * Get the Thai localized description lines for this ability (player-specific).
     */
    List<String> getDescriptionTh(Player player);

    /**
     * Get the trigger type of this ability.
     */
    PetAbilityType getType();
}
