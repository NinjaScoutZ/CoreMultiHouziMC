package com.houzicore.shared.common.util;

public class StreakCalculator {

    /**
     * Calculates the reward multiplier using logarithmic regression
     * @param currentStreak The consecutive login/game completion streak
     * @return Double multiplier starting at 1.0
     */
    public static double calculateMultiplier(int currentStreak) {
        if (currentStreak <= 0) return 1.0;
        
        // Linear growth for first 20 streaks (0.05 per streak up to +1.0)
        double multiplier = 1.0 + Math.min(1.0, currentStreak * 0.05); 
        
        // Logarithmic scaling to prevent infinite unscaled growth
        if (currentStreak > 20) {
            multiplier += Math.log10(currentStreak - 19) * 0.5;
        }
        
        // Cap the multiplier at 500% (5.0)
        return Math.min(5.0, multiplier);
    }

    /**
     * Calculates the baseline essence infused with the user's streak
     */
    public static int calculateEssenceBonus(int baseEssence, int currentStreak) {
        return (int) Math.round(baseEssence * calculateMultiplier(currentStreak));
    }
}
