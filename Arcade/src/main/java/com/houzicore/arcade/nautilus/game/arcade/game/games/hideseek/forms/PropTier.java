package com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.forms;

public enum PropTier {
    COMMON(1.0, 20.0),
    RARE(2.0, 20.0),
    TINY(3.0, 6.0);

    private final double pointMultiplier;
    private final double maxHealth;

    PropTier(double pointMultiplier, double maxHealth) {
        this.pointMultiplier = pointMultiplier;
        this.maxHealth = maxHealth;
    }

    public double getPointMultiplier() {
        return pointMultiplier;
    }

    public double getMaxHealth() {
        return maxHealth;
    }
}
