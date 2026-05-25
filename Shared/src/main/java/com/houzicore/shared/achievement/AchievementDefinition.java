package com.houzicore.shared.achievement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Defines a single achievement with its tiers, category, and bilingual text.
 *
 * <pre>
 * AchievementDefinition.builder()
 *     .id("skywars_wins")
 *     .nameEN("Island Conqueror").nameTH("ผู้พิชิตเกาะ")
 *     .descEN("Win games of SkyWars").descTH("ชนะเกม SkyWars")
 *     .type(AchievementType.TIERED)
 *     .category(GameCategory.SKYWARS)
 *     .tier(new AchievementTier(1, 10, 5))    // Tier I: win 10 → 5 pts
 *     .tier(new AchievementTier(2, 50, 10))   // Tier II: win 50 → 10 pts
 *     .tier(new AchievementTier(3, 200, 20))  // Tier III: win 200 → 20 pts
 *     .build();
 * </pre>
 *
 * Ported from: net.swofty.type.generic.achievement.AchievementDefinition
 */
public class AchievementDefinition {

    private final String id;
    private final String nameEN;
    private final String nameTH;
    private final String descEN;
    private final String descTH;
    private final AchievementType type;
    private final GameCategory category;
    private final List<AchievementTier> tiers;
    private final int points; // สำหรับ ONE_TIME (ไม่มี tiers)

    private AchievementDefinition(Builder b) {
        this.id = b.id;
        this.nameEN = b.nameEN;
        this.nameTH = b.nameTH;
        this.descEN = b.descEN;
        this.descTH = b.descTH;
        this.type = b.type;
        this.category = b.category;
        this.tiers = Collections.unmodifiableList(b.tiers);
        this.points = b.points;
    }

    // --- Getters ---
    public String getId() { return id; }
    public String getName(boolean thai) { return thai ? nameTH : nameEN; }
    public String getDesc(boolean thai) { return thai ? descTH : descEN; }
    public AchievementType getType() { return type; }
    public GameCategory getCategory() { return category; }
    public List<AchievementTier> getTiers() { return tiers; }

    /** Total achievement points across all tiers, or single points for ONE_TIME. */
    public int getTotalPoints() {
        if (type == AchievementType.ONE_TIME) return points;
        return tiers.stream().mapToInt(AchievementTier::points).sum();
    }

    /** Highest tier number. Returns 0 for ONE_TIME. */
    public int getMaxTier() {
        return tiers.stream().mapToInt(AchievementTier::tier).max().orElse(0);
    }

    /** Goal for a specific tier. */
    public int getGoalForTier(int tier) {
        return tiers.stream().filter(t -> t.tier() == tier)
                .mapToInt(AchievementTier::goal).findFirst().orElse(0);
    }

    /** Cumulative points up to and including a tier. */
    public int getPointsUpToTier(int tier) {
        return tiers.stream().filter(t -> t.tier() <= tier)
                .mapToInt(AchievementTier::points).sum();
    }

    // --- Builder ---
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id;
        private String nameEN = "", nameTH = "";
        private String descEN = "", descTH = "";
        private AchievementType type = AchievementType.ONE_TIME;
        private GameCategory category = GameCategory.GENERAL;
        private final List<AchievementTier> tiers = new ArrayList<>();
        private int points = 0;

        public Builder id(String id) { this.id = id; return this; }
        public Builder nameEN(String n) { this.nameEN = n; return this; }
        public Builder nameTH(String n) { this.nameTH = n; return this; }
        public Builder descEN(String d) { this.descEN = d; return this; }
        public Builder descTH(String d) { this.descTH = d; return this; }
        public Builder type(AchievementType t) { this.type = t; return this; }
        public Builder category(GameCategory c) { this.category = c; return this; }
        public Builder tier(AchievementTier t) { this.tiers.add(t); return this; }
        public Builder points(int p) { this.points = p; return this; }

        public AchievementDefinition build() {
            if (id == null || id.isEmpty()) throw new IllegalStateException("Achievement ID required");
            return new AchievementDefinition(this);
        }
    }
}
