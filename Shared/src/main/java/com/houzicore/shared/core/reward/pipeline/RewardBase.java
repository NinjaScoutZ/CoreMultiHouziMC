package com.houzicore.shared.core.reward.pipeline;

import org.bukkit.entity.Player;

/**
 * Standardized Reward Pipeline Architecture.
 * Replaces the legacy raw reward systems (e.g., raw Essence/Gem distribution).
 * Supports the distribution of Essence, Items, and Cosmetics with multipliers and animations.
 */
public abstract class RewardBase {

    private final String _reason;
    private final double _amount;

    public RewardBase(String reason, double amount) {
        _reason = reason;
        _amount = amount;
    }

    /**
     * @return The display name or reason for this reward (e.g. "Winning Team")
     */
    public String getReason() {
        return _reason;
    }

    /**
     * @return The base amount of this reward before multipliers
     */
    public double getAmount() {
        return _amount;
    }

    /**
     * Calculate the final amount after applying any active game multipliers.
     */
    public double getCalculatedAmount(double multiplier) {
        return supportsMultiplier() ? (_amount * multiplier) : _amount;
    }

    /**
     * Should this reward scale with game multipliers/boosters?
     */
    public abstract boolean supportsMultiplier();

    /**
     * Distribute the reward to the given player.
     * @param player The receiving player
     * @param multiplier The current active multiplier
     */
    public abstract void giveReward(Player player, double multiplier);

    /**
     * Play visual/audio effects signifying the delivery of the reward.
     * @param player The receiving player
     * @param location Optional location to play the effect
     */
    public abstract void playAnimation(Player player, org.bukkit.Location location);

    /**
     * @return Formatted string to display in the post-game Reward Summary UI.
     */
    public abstract String getSummaryString(double multiplier);
}
