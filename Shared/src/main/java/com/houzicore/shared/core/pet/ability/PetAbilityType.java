package com.houzicore.shared.core.pet.ability;

/**
 * Defines when a {@link PetAbility} activates.
 */
public enum PetAbilityType {
    /** Always active while the pet is equipped. */
    PASSIVE,
    /** Triggers when the owner gets a kill in an Arcade game. */
    ON_KILL,
    /** Triggers when the owner wins an Arcade game. */
    ON_WIN,
    /** Triggers when an Arcade game starts. */
    ON_GAME_START
}
