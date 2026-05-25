package com.houzicore.arcade;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Ensures that GameMapRequirements entries are valid and consistent.
 * Guards against accidentally registering null or empty requirements.
 */
class GameMapRequirementsRegistryTest
{
    @Test
    void allRegisteredTypesHaveValidRequirements()
    {
        for (GameType type : GameMapRequirements.getRegisteredTypes())
        {
            GameMapRequirements.GameReqs reqs = GameMapRequirements.getRequirements(type);
            assertNotNull(reqs, "Null requirements for registered type: " + type);
            assertNotNull(reqs.gameDescription(), "Null description for: " + type);
            assertFalse(reqs.gameDescription().isEmpty(), "Empty description for: " + type);
            assertNotNull(reqs.teams(), "Null teams list for: " + type);
            assertFalse(reqs.teams().isEmpty(), "No teams defined for: " + type);
            assertNotNull(reqs.dataLocs(), "Null dataLocs list for: " + type);
            assertNotNull(reqs.customLocs(), "Null customLocs list for: " + type);
        }
    }

    @Test
    void keyMinigamesAreRegistered()
    {
        assertTrue(GameMapRequirements.hasRequirements(GameType.Bedwars), "Bedwars not registered");
        assertTrue(GameMapRequirements.hasRequirements(GameType.SpeedBuilders), "SpeedBuilders not registered");
        assertTrue(GameMapRequirements.hasRequirements(GameType.SuperSmash), "SuperSmash not registered");
        assertTrue(GameMapRequirements.hasRequirements(GameType.SmashTeams), "SmashTeams not registered");
        assertTrue(GameMapRequirements.hasRequirements(GameType.PropRush), "PropRush not registered");
        assertTrue(GameMapRequirements.hasRequirements(GameType.Skywars), "Skywars not registered");
        assertTrue(GameMapRequirements.hasRequirements(GameType.CastleSiege), "CastleSiege not registered");
    }

    @Test
    void bedwarsHasComprehensiveRequirements()
    {
        GameMapRequirements.GameReqs reqs = GameMapRequirements.getRequirements(GameType.Bedwars);
        assertNotNull(reqs);

        // Should have at least 4 teams
        assertTrue(reqs.teams().size() >= 4, "Bedwars should have 4+ teams but had " + reqs.teams().size());

        // Should have data locs (BLACK, GRAY, bed positions)
        assertTrue(reqs.dataLocs().size() >= 4, "Bedwars should have 4+ data locs but had " + reqs.dataLocs().size());

        // Should have custom locs (GEN, SHOP, POINT)
        assertTrue(reqs.customLocs().size() >= 4, "Bedwars should have 4+ custom locs but had " + reqs.customLocs().size());
    }

    @Test
    void speedBuildersHasRequiredMarkers()
    {
        GameMapRequirements.GameReqs reqs = GameMapRequirements.getRequirements(GameType.SpeedBuilders);
        assertNotNull(reqs);

        // Should have RED and YELLOW data locs
        assertTrue(reqs.dataLocs().stream().anyMatch(r -> r.color().equals("RED")),
                "SpeedBuilders should have RED data loc");
        assertTrue(reqs.dataLocs().stream().anyMatch(r -> r.color().equals("YELLOW")),
                "SpeedBuilders should have YELLOW data loc");

        // Should have custom locs for builds
        assertFalse(reqs.customLocs().isEmpty(), "SpeedBuilders should have custom locs for builds");
    }
}
