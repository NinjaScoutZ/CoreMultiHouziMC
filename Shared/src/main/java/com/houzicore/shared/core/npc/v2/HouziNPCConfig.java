package com.houzicore.shared.core.npc.v2;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;

/**
 * Defines the static configuration for a HouziNPC.
 */
public interface HouziNPCConfig {

    /**
     * The lines to display above the NPC's head. Bottom-up order or Top-down depending on HologramManager.
     * We will process it in the order given (first string is highest).
     */
    String[] getHolograms();

    /**
     * The location where this NPC should spawn.
     */
    Location getLocation();

    /**
     * The entity type to spawn. Defaults to PLAYER if signature/texture are provided.
     */
    default EntityType getEntityType() {
        return EntityType.VILLAGER;
    }

    /**
     * Base64 skin signature. Return null if not a player NPC.
     */
    default String getSignature() {
        return null;
    }

    /**
     * Base64 skin texture. Return null if not a player NPC.
     */
    default String getTexture() {
        return null;
    }

    /**
     * Whether the NPC should rotate its head to look at nearby players.
     */
    default boolean isLookAtPlayer() {
        return false;
    }
}
