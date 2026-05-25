package com.houzicore.shared.achievement;

/**
 * Types of achievements supported by the framework.
 */
public enum AchievementType {
    /** Complete once, get reward once. Example: "First Win" */
    ONE_TIME,
    /** Multiple tiers with increasing goals. Example: "Win 10/50/200 games" */
    TIERED,
    /** Available only during a seasonal event. Resets each season. */
    SEASONAL
}
