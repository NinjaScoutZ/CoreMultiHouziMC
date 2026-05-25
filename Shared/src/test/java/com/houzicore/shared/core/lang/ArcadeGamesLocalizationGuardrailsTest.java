package com.houzicore.shared.core.lang;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Localization guardrails for Bedwars, Speed Builders, and Super Smash Mobs.
 * Ensures that both EN and TH catalogs contain matching game_menu keys
 * so no missing translations slip through.
 */
class ArcadeGamesLocalizationGuardrailsTest
{
    private static final List<String> SPEED_BUILDERS_KEYS = List.of(
            "game_menu.speed_builders.name",
            "game_menu.speed_builders.desc.1",
            "game_menu.speed_builders.desc.2",
            "game_menu.speed_builders.desc.3"
    );

    private static final List<String> BEDWARS_KEYS = List.of(
            "game_menu.bedwars.name",
            "game_menu.bedwars.desc.1",
            "game_menu.bedwars.desc.2",
            "game_menu.bedwars.desc.3"
    );

    @Test
    void speedBuildersKeysExistInBothCatalogs()
    {
        Map<String, String> en = DefaultLang.DEFAULTS;
        Map<String, String> th = DefaultLangTh.DEFAULTS;

        for (String key : SPEED_BUILDERS_KEYS)
        {
            assertTrue(en.containsKey(key), "Missing EN key: " + key);
            assertTrue(th.containsKey(key), "Missing TH key: " + key);
        }
    }

    @Test
    void bedwarsKeysExistInBothCatalogs()
    {
        Map<String, String> en = DefaultLang.DEFAULTS;
        Map<String, String> th = DefaultLangTh.DEFAULTS;

        for (String key : BEDWARS_KEYS)
        {
            assertTrue(en.containsKey(key), "Missing EN key: " + key);
            assertTrue(th.containsKey(key), "Missing TH key: " + key);
        }
    }

    @Test
    void enAndThHaveSameGameMenuKeys()
    {
        // Verify every game_menu.* key in EN also exists in TH and vice versa
        Map<String, String> en = DefaultLang.DEFAULTS;
        Map<String, String> th = DefaultLangTh.DEFAULTS;

        for (String key : en.keySet())
        {
            if (key.startsWith("game_menu."))
            {
                assertTrue(th.containsKey(key), "game_menu key in EN but missing in TH: " + key);
            }
        }

        for (String key : th.keySet())
        {
            if (key.startsWith("game_menu."))
            {
                assertTrue(en.containsKey(key), "game_menu key in TH but missing in EN: " + key);
            }
        }
    }
}
