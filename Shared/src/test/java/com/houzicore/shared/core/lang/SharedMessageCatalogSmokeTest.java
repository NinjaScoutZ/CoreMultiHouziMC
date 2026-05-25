package com.houzicore.shared.core.lang;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class SharedMessageCatalogSmokeTest {

    @Test
    void bundledCoreCatalogsParseAndExposeExpectedKeys() throws IOException {
        assertHasKey("messages_en.yml", "compass.name");
        assertHasKey("messages_th.yml", "compass.name");
        assertHasKey("messages/en/prop_rush.yml", "prop_rush.feedback.scanner_pulse_found");
        assertHasKey("messages/th/prop_rush.yml", "prop_rush.feedback.scanner_pulse_found");
        assertHasKey("messages/en/treasure.yml", "treasure.ui.title");
        assertHasKey("messages/th/treasure.yml", "treasure.ui.title");
    }

    private static void assertHasKey(String resourcePath, String key) throws IOException {
        FileConfiguration configuration = loadCatalog(resourcePath);
        assertFalse(configuration.getKeys(true).isEmpty(), "Catalog parsed empty: " + resourcePath);
        assertTrue(configuration.contains(key), "Missing key " + key + " in " + resourcePath);
    }

    private static FileConfiguration loadCatalog(String resourcePath) throws IOException {
        try (InputStream stream = SharedMessageCatalogSmokeTest.class.getClassLoader().getResourceAsStream(resourcePath)) {
            assertNotNull(stream, "Missing resource: " + resourcePath);
            return YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
        }
    }
}
