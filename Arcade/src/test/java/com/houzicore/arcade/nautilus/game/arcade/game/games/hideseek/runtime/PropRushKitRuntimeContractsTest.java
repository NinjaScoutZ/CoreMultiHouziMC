package com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.runtime;

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
import java.util.TreeSet;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

class PropRushKitRuntimeContractsTest
{
    private static final List<String> LIVE_KITS = List.of(
            "chameleon", "ghost", "bomb_bug", "locksmith", "mimic",
            "tracker", "destroyer", "trapper", "bloodhound", "saboteur",
            "bounty_hunter", "exorcist", "falconer", "warden");

    private static final List<String> LIVE_PERKS = List.of(
            "decoy", "phase_shift", "bomb_shell", "secret_passage", "mirror_image",
            "scanner_pulse", "double_jump", "flare", "speed_ii", "bloodhound_sense",
            "smoke_bomb", "bounty_dash", "purge_pulse", "sky_sweep", "echo_sentry");

    @Test
    void liveRosterExcludesBrokenTricksterAndKeepsExpectedLiveKits() throws IOException
    {
        String rosterSource = Files.readString(Path.of("src", "main", "java", "com", "houzicore", "arcade",
                "nautilus", "game", "arcade", "game", "games", "hideseek", "runtime",
                "PropRushKitRoster.java"), StandardCharsets.UTF_8);

        assertFalse(rosterSource.contains("new KitTrickster("),
                "Trickster should stay out of the live roster until it has a complete active contract");

        for (String liveKit : List.of(
                "KitChameleon", "KitGhost", "KitBombBug", "KitLocksmith", "KitMimic",
                "KitTracker", "KitDestroyer", "KitTrapper", "KitBloodhound", "KitSaboteur",
                "KitBountyHunter", "KitExorcist", "KitFalconer", "KitWarden"))
        {
            assertTrue(rosterSource.contains("new " + liveKit + "(manager)"),
                    "Missing live roster constructor: " + liveKit);
        }
    }

    @Test
    void hideSeekDelegatesLoadoutsAndDropsLegacyGrantBranches() throws IOException
    {
        String hideSeekSource = Files.readString(Path.of("src", "main", "java", "com", "houzicore", "arcade",
                "nautilus", "game", "arcade", "game", "games", "hideseek", "HideSeek.java"),
                StandardCharsets.UTF_8);

        assertTrue(hideSeekSource.contains("PropRushKitLoadoutService.applyHiderLoadout(player, GetKit(player));"));
        assertTrue(hideSeekSource.contains("PropRushKitLoadoutService.applyHunterLoadout(player, GetKit(player));"));
        assertFalse(hideSeekSource.contains("kitName.equals("),
                "HideSeek should not own Prop Rush loadouts through string-based kit branching anymore");
        assertFalse(hideSeekSource.contains("createAbilityItem("),
                "Legacy duplicated ability-item helpers should not remain in HideSeek");
        assertFalse(hideSeekSource.contains("tryBridgeMovingBlockDisplayHit("),
                "HideSeek should not restore the temporary BlockDisplay melee bridge");
        assertFalse(hideSeekSource.contains("findBridgeableMovingBlockHider("),
                "HideSeek should not target moving BlockDisplay disguises as melee bridge hits");
        assertFalse(hideSeekSource.contains("public void onTauntInteract("),
                "HideSeek should not keep the legacy left-click taunt path alive");
        assertEquals(1, countOccurrences(hideSeekSource, "item.getType() == Material.CROSSBOW"),
                "Bounty Dash should have exactly one crossbow handler path");
    }

    @Test
    void runtimeContractSourceListsLiveKitsAndExplicitlyDisablesTrickster() throws IOException
    {
        String contractSource = Files.readString(Path.of("src", "main", "java", "com", "houzicore", "arcade",
                "nautilus", "game", "arcade", "game", "games", "hideseek", "runtime",
                "PropRushKitRuntimeContracts.java"), StandardCharsets.UTF_8);

        for (String kitKey : LIVE_KITS)
        {
            assertTrue(contractSource.contains("builder(\"" + kitKey + "\""),
                    "Missing runtime contract entry for live kit: " + kitKey);
        }

        assertTrue(contractSource.contains("List.of(\"trickster\")"));
        assertTrue(contractSource.contains("List.of(\"trickery\", \"blinding_strike\")"));
        assertTrue(contractSource.contains("addSupplementalItem(1, Material.BOW, 1)"));
        assertTrue(contractSource.contains("addSupplementalItem(2, Material.COBWEB, 3)"));
    }

    @Test
    void liveRuntimeKeysMapToLocalizedKitAndPerkCatalogs() throws IOException
    {
        Map<String, String> en = flatten(loadCatalog("messages/en/prop_rush.yml"));
        Map<String, String> th = flatten(loadCatalog("messages/th/prop_rush.yml"));

        for (String kitKey : LIVE_KITS)
        {
            assertCatalogKey(en, th, "prop_rush.kit." + kitKey + ".name");
            assertCatalogKey(en, th, "prop_rush.kit." + kitKey + ".desc");
            assertCatalogKey(en, th, "prop_rush.summary.kit_compact." + kitKey);
        }

        for (String perkKey : LIVE_PERKS)
        {
            assertCatalogKey(en, th, "prop_rush.perk." + perkKey + ".name");
            assertCatalogKey(en, th, "prop_rush.perk." + perkKey + ".desc");
        }
    }

    @Test
    void deliveryReferenceDocumentsTheReducedLiveRoster() throws IOException
    {
        String deliveryDoc = Files.readString(Path.of("..", "..", "docs",
                "structural_update_2026-04-20_hideandseek_delivery_reference.md"), StandardCharsets.UTF_8);

        assertTrue(deliveryDoc.contains("Live kit roster in code: 14 kits"));
        assertTrue(deliveryDoc.contains("`Trickster` is intentionally out of the live roster"));
    }

    private static FileConfiguration loadCatalog(String resourcePath) throws IOException
    {
        try (InputStream stream = PropRushKitRuntimeContractsTest.class.getClassLoader().getResourceAsStream(resourcePath))
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

    private static void assertCatalogKey(Map<String, String> en, Map<String, String> th, String key)
    {
        assertTrue(en.containsKey(key), "Missing EN key: " + key);
        assertTrue(th.containsKey(key), "Missing TH key: " + key);
    }

    private static int countOccurrences(String source, String needle)
    {
        int count = 0;
        int index = 0;
        while ((index = source.indexOf(needle, index)) >= 0)
        {
            count++;
            index += needle.length();
        }
        return count;
    }
}
