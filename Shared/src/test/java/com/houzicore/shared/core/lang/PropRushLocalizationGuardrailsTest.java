package com.houzicore.shared.core.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class PropRushLocalizationGuardrailsTest {

    @Test
    void englishAndThaiCatalogsHaveMatchingKeys() throws IOException {
        Set<String> enKeys = flatten(loadCatalog("messages/en/prop_rush.yml")).keySet();
        Set<String> thKeys = flatten(loadCatalog("messages/th/prop_rush.yml")).keySet();

        assertEquals(enKeys, thKeys, "Prop Rush EN/TH catalogs drifted");
    }

    @Test
    void propRushCatalogCoversLiveRosterKitsAndPerks() throws IOException {
        Map<String, String> en = flatten(loadCatalog("messages/en/prop_rush.yml"));
        Map<String, String> th = flatten(loadCatalog("messages/th/prop_rush.yml"));

        List<String> kits = List.of(
                "chameleon", "ghost", "bomb_bug", "locksmith", "mimic",
                "tracker", "destroyer", "trapper", "bloodhound", "saboteur",
                "bounty_hunter", "exorcist", "falconer", "warden");
        List<String> perks = List.of(
                "decoy", "phase_shift", "bomb_shell",
                "secret_passage", "mirror_image", "scanner_pulse", "double_jump",
                "flare", "speed_ii", "bloodhound_sense", "smoke_bomb", "bounty_dash",
                "purge_pulse", "sky_sweep", "echo_sentry");

        for (String kit : kits) {
            assertCatalogKey(en, th, "prop_rush.kit." + kit + ".name");
            assertCatalogKey(en, th, "prop_rush.kit." + kit + ".desc");
            assertCatalogKey(en, th, "prop_rush.summary.kit_compact." + kit);
        }

        for (String perk : perks) {
            assertCatalogKey(en, th, "prop_rush.perk." + perk + ".name");
            assertCatalogKey(en, th, "prop_rush.perk." + perk + ".desc");
        }
    }

    @Test
    void keyRuntimeSurfacesDoNotReintroduceBannedHardcodedStrings() throws IOException {
        assertFileDoesNotContain(
                Path.of("..", "Arcade", "src", "main", "java", "com", "houzicore", "arcade", "nautilus", "game",
                        "arcade", "game", "games", "hideseek", "HideSeek.java"),
                List.of(
                        "Fake Sound Ping deployed.",
                        "Search Relay recharging",
                        "You must be in an empty space to place a Decoy!",
                        "Mirror Image needs more room to split.",
                        "You destroyed a Decoy!",
                        "Pulse detected a Hider nearby!",
                        "Scent detected! Target revealed.",
                        "You were revealed by a Hunter's Flare!",
                        "A Gold Cache has spawned nearby."));

        assertFileDoesNotContain(
                Path.of("..", "Arcade", "src", "main", "java", "com", "houzicore", "arcade", "nautilus", "game",
                        "arcade", "managers", "GameLobbyManager.java"),
                List.of(
                        "LangManager.get().isThai(player) ? \"คิทความสำเร็จ\" : \"Achievement Kit\"",
                        "LangManager.get().isThai(player) ? \"เอสเซนซ์\" : \"Essence\""));

        assertFileDoesNotContain(
                Path.of("..", "Lobby", "src", "main", "java", "com", "houzicore", "lobby", "hub", "server", "ui",
                        "ServerGameMenu.java"),
                List.of("Micro Battles", "Turf Wars", "Dragon Escape", "Death Tag"));
    }

    private static FileConfiguration loadCatalog(String resourcePath) throws IOException {
        try (InputStream stream = PropRushLocalizationGuardrailsTest.class.getClassLoader().getResourceAsStream(resourcePath)) {
            assertNotNull(stream, "Missing resource: " + resourcePath);
            return YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
        }
    }

    private static Map<String, String> flatten(FileConfiguration configuration) {
        Map<String, String> flat = new java.util.LinkedHashMap<>();
        for (String key : new TreeSet<>(configuration.getKeys(true))) {
            if (configuration.isString(key)) {
                flat.put(key, configuration.getString(key));
            } else if (configuration.isList(key)) {
                flat.put(key, String.join("\n", configuration.getStringList(key)));
            }
        }
        return flat;
    }

    private static void assertCatalogKey(Map<String, String> en, Map<String, String> th, String key) {
        assertTrue(en.containsKey(key), "Missing EN key: " + key);
        assertTrue(th.containsKey(key), "Missing TH key: " + key);
    }

    private static void assertFileDoesNotContain(Path relativePath, List<String> bannedSnippets) throws IOException {
        String content = Files.readString(relativePath.toAbsolutePath().normalize(), StandardCharsets.UTF_8);
        for (String banned : bannedSnippets) {
            assertFalse(content.contains(banned), "Found banned hardcoded snippet in " + relativePath + ": " + banned);
        }
    }
}
