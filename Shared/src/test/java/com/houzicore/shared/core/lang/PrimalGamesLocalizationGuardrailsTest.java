package com.houzicore.shared.core.lang;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class PrimalGamesLocalizationGuardrailsTest
{
    private static final List<String> REQUIRED_KEYS = List.of(
            "primal_games.game.title",
            "primal_games.game.desc",
            "primal_games.game.desc_teams",
            "primal_games.scoreboard.phase",
            "primal_games.scoreboard.time",
            "primal_games.scoreboard.phase_survive",
            "primal_games.scoreboard.phase_border_closing",
            "primal_games.scoreboard.phase_deathmatch",
            "primal_games.scoreboard.top_killers",
            "primal_games.scoreboard.no_kills",
            "primal_games.scoreboard.game_end",
            "primal_games.bossbar.border_phase1",
            "primal_games.bossbar.border_phase2",
            "primal_games.bossbar.deathmatch",
            "primal_games.bossbar.disaster_toxic_gas",
            "primal_games.bossbar.disaster_generic",
            "primal_games.actionbar.deathmatch",
            "primal_games.actionbar.border_warn",
            "primal_games.actionbar.border_closing_info",
            "primal_games.actionbar.normal",
            "primal_games.title.prepare_go",
            "primal_games.title.prepare_subtitle",
            "primal_games.title.game_start",
            "primal_games.title.game_start_subtitle",
            "primal_games.title.survive",
            "primal_games.title.survive_subtitle",
            "primal_games.title.deathmatch",
            "primal_games.title.deathmatch_subtitle",
            "primal_games.title.airdrop",
            "primal_games.title.airdrop_subtitle",
            "primal_games.announce.header",
            "primal_games.announce.game_started",
            "primal_games.announce.tip_explore",
            "primal_games.announce.tip_runes",
            "primal_games.announce.tip_gas",
            "primal_games.announce.border_phase2",
            "primal_games.announce.border_phase3",
            "primal_games.item.bandage_name",
            "primal_games.item.bandage_lore_use",
            "primal_games.item.bandage_lore_effect",
            "primal_games.item.bandage_used",
            "primal_games.item.compass_name",
            "primal_games.item.compass_uses",
            "primal_games.item.compass_lore1",
            "primal_games.item.compass_lore2",
            "primal_games.item.compass_located",
            "primal_games.item.compass_uses_remaining",
            "primal_games.item.compass_no_uses",
            "primal_games.item.compass_broken",
            "primal_games.item.compass_no_target",
            "primal_games.trader.name",
            "primal_games.trader.menu_title",
            "primal_games.deathmatch.countdown_pre",
            "primal_games.deathmatch.countdown_live",
            "primal_games.deathmatch.started");

    @Test
    void requiredSurvivalGamesKeysExistInEnglishAndThai() throws IOException
    {
        Map<String, String> en = flatten(loadCatalog("messages/en/primal_games.yml"));
        Map<String, String> th = flatten(loadCatalog("messages/th/primal_games.yml"));

        for (String key : REQUIRED_KEYS)
        {
            assertTrue(en.containsKey(key), "Missing EN key: " + key);
            assertTrue(th.containsKey(key), "Missing TH key: " + key);
        }
    }

    @Test
    void legacyManualDeathmatchAndGasKeysAreGone() throws IOException
    {
        Map<String, String> en = flatten(loadCatalog("messages/en/primal_games.yml"));
        Map<String, String> th = flatten(loadCatalog("messages/th/primal_games.yml"));

        for (String legacyKey : List.of(
                "primal_games.announce.dm_vote",
                "primal_games.deathmatch.triggered",
                "primal_games.deathmatch.countdown_start",
                "primal_games.scoreboard.phase_gas_closing",
                "primal_games.bossbar.safe_zone",
                "primal_games.bossbar.gas_closing_timer",
                "primal_games.bossbar.gas_closing",
                "primal_games.actionbar.gas_warning",
                "primal_games.actionbar.gas_closing_info"))
        {
            assertFalse(en.containsKey(legacyKey), "Legacy EN key should be removed: " + legacyKey);
            assertFalse(th.containsKey(legacyKey), "Legacy TH key should be removed: " + legacyKey);
        }
    }

    private static FileConfiguration loadCatalog(String resourcePath) throws IOException
    {
        try (InputStream stream = PrimalGamesLocalizationGuardrailsTest.class.getClassLoader().getResourceAsStream(resourcePath))
        {
            assertNotNull(stream, "Missing resource: " + resourcePath);
            return YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
        }
    }

    private static Map<String, String> flatten(FileConfiguration configuration)
    {
        Map<String, String> flat = new java.util.LinkedHashMap<String, String>();
        for (String key : new TreeSet<String>(configuration.getKeys(true)))
        {
            if (configuration.isString(key))
            {
                flat.put(key, configuration.getString(key));
            }
            else if (configuration.isList(key))
            {
                flat.put(key, String.join("\n", configuration.getStringList(key)));
            }
        }
        return flat;
    }
}
