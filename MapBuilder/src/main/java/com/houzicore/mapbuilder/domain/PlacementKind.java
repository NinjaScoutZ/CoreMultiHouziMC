package com.houzicore.mapbuilder.domain;

/**
 * Defines how a data point type is placed in a map session.
 *
 * SINGLE      — only one instance allowed; placing again replaces the previous.
 * MULTI       — unlimited instances; each click places an additional point.
 * PAIR_REGION — exactly two points define a rectangular zone; first click anchors,
 *               second click commits the pair (e.g. Fishing Zone, Farm Zone).
 * DIRECTIONAL — like MULTI but captures the player's yaw/pitch at placement time
 *               (useful for spawn points that need a facing direction).
 */
public enum PlacementKind {
    SINGLE,
    MULTI,
    PAIR_REGION,
    DIRECTIONAL
}
