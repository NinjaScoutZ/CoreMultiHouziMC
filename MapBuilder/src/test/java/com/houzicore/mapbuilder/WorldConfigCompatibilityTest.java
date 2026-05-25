package com.houzicore.mapbuilder;

import com.houzicore.mapbuilder.schema.MapSchema;
import com.houzicore.mapbuilder.schema.MapSchemaExporter;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Golden-file test: ensures that MapSchemaExporter.serializeToString() produces
 * byte/line identical output to the legacy WorldConfigExporter.buildConfigContent().
 *
 * This guards Arcade ParseData from silent format regressions.
 * If this test fails, the change to MapSchemaExporter broke the format and
 * Arcade-side ParseData will silently fail to read maps.
 */
class WorldConfigCompatibilityTest {

    /**
     * Golden reference output — manually verified against the legacy buildConfigContent()
     * using an equivalent fixture MapSession with the same data.
     */
    private static final String GOLDEN_OUTPUT =
            "MAP_NAME:TestMap\n" +
            "MAP_AUTHOR:TestAuthor\n" +
            "MIN_X:-10\n" +
            "MAX_X:10\n" +
            "MIN_Z:-8\n" +
            "MAX_Z:8\n" +
            "MIN_Y:60\n" +
            "MAX_Y:80\n" +
            "TEAM_NAME:Blue\n" +
            "TEAM_DIRECTION:0\n" +
            "TEAM_SPAWNS:5,64,0:6,64,0\n" +
            "TEAM_NAME:Red\n" +
            "TEAM_DIRECTION:180\n" +
            "TEAM_SPAWNS:-5,64,0:-6,64,0\n" +
            "DATA_NAME:ZONE_ARENA\n" +
            "DATA_LOCS:0,64,0\n" +
            "CUSTOM_NAME:FLAG_POST\n" +
            "CUSTOM_LOCS:0,65,3\n" +
            "BLOCK_DISPLAY:PILLAR_A\n" +
            "BLOCK_DISPLAY_LOCS:2,64,2\n";

    @Test
    void testOutputMatchesGoldenFile() {
        MapSchema schema = buildFixtureSchema();
        String actual = MapSchemaExporter.serializeToString(schema);

        assertEquals(GOLDEN_OUTPUT, actual,
                "WorldConfig.dat format must be byte-identical to preserve Arcade ParseData compatibility. " +
                "If this fails, update ParseData reader and golden reference together.");
    }

    // -------------------------------------------------------------------------

    private MapSchema buildFixtureSchema() {
        MapSchema.BoundingBox bounds = new MapSchema.BoundingBox(-10, 60, -8, 10, 80, 8);

        Map<String, List<MapSchema.SchemaPoint>> points = new LinkedHashMap<>();

        // Teams
        List<MapSchema.SchemaPoint> blueSpawns = new ArrayList<>();
        blueSpawns.add(new MapSchema.SchemaPoint(5, 64, 0));
        blueSpawns.add(new MapSchema.SchemaPoint(6, 64, 0));
        points.put("TEAM_NAME:Blue", blueSpawns);

        List<MapSchema.SchemaPoint> redSpawns = new ArrayList<>();
        redSpawns.add(new MapSchema.SchemaPoint(-5, 64, 0));
        redSpawns.add(new MapSchema.SchemaPoint(-6, 64, 0));
        points.put("TEAM_NAME:Red", redSpawns);

        // Data points
        List<MapSchema.SchemaPoint> arenaCenter = new ArrayList<>();
        arenaCenter.add(new MapSchema.SchemaPoint(0, 64, 0));
        points.put("DATA_NAME:ZONE_ARENA", arenaCenter);

        // Custom
        List<MapSchema.SchemaPoint> flagPost = new ArrayList<>();
        flagPost.add(new MapSchema.SchemaPoint(0, 65, 3));
        points.put("CUSTOM_NAME:FLAG_POST", flagPost);

        // Block display
        List<MapSchema.SchemaPoint> pillar = new ArrayList<>();
        pillar.add(new MapSchema.SchemaPoint(2, 64, 2));
        points.put("BLOCK_DISPLAY:PILLAR_A", pillar);

        return new MapSchema("TestMap", "TestAuthor", "TestGameType", bounds, points);
    }
}
