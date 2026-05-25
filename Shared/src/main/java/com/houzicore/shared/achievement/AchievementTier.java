package com.houzicore.shared.achievement;

import com.houzicore.shared.common.util.UtilText;

/**
 * Represents one tier of a tiered achievement.
 * Example: "Island Conqueror I" (win 10), "Island Conqueror II" (win 50), etc.
 *
 * Ported from: net.swofty.type.generic.achievement.AchievementTier
 *
 * @param tier   The tier number (1, 2, 3, ...)
 * @param goal   How many actions needed to complete this tier
 * @param points Achievement points rewarded for completing this tier
 */
public record AchievementTier(int tier, int goal, int points) {

    /**
     * Get the Roman numeral representation of this tier.
     * @return "I", "II", "III", etc.
     */
    public String getRomanNumeral() {
        return UtilText.toRomanNumeral(tier);
    }
}
