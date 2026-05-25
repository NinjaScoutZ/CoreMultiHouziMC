package com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class PrimalGamesRuntimeGuardrailsTest
{
    @Test
    void runtimeDropsLegacyManualDeathmatchAndHardcodedItemText() throws IOException
    {
        String primalGames = read("src", "main", "java", "com", "houzicore", "arcade",
                "nautilus", "game", "arcade", "game", "games", "primalgames", "PrimalGames.java");

        assertFalse(primalGames.contains("PlayerCommandPreprocessEvent"),
                "PrimalGames should not keep the legacy manual /dm activation path");
        assertFalse(primalGames.contains("\"/dm\""),
                "PrimalGames should not keep the legacy manual /dm command");
        assertFalse(primalGames.contains("Deathmatch cannot be started now."),
                "Legacy manual deathmatch feedback should be gone");
        assertFalse(primalGames.contains("You are not in the game."),
                "Legacy /dm feedback should be gone");
        assertFalse(primalGames.contains("contains(\"Bandage\")"),
                "Bandage consumption should not depend on English display-name matching");
        assertTrue(primalGames.contains("primal_games.item.bandage_used"));
        assertTrue(primalGames.contains("primal_games.item.compass_located"));
        assertTrue(primalGames.contains("primal_games.item.compass_uses_remaining"));
        assertTrue(primalGames.contains("primal_games.item.compass_no_uses"));
        assertTrue(primalGames.contains("primal_games.item.compass_no_target"));
        assertTrue(primalGames.contains("primal_games.item.compass_broken"));
    }

    @Test
    void runtimeUsesLocalizedSurvivalGamesContractsAcrossOwners() throws IOException
    {
        String primalGamesLang = read("src", "main", "java", "com", "houzicore", "arcade",
                "nautilus", "game", "arcade", "game", "games", "primalgames", "lang", "PrimalGamesLang.java");
        String lootTableManager = read("src", "main", "java", "com", "houzicore", "arcade",
                "nautilus", "game", "arcade", "game", "games", "primalgames", "LootTableManager.java");
        String wanderingTraderManager = read("src", "main", "java", "com", "houzicore", "arcade",
                "nautilus", "game", "arcade", "game", "games", "primalgames", "WanderingTraderManager.java");
        String soloPrimalGames = read("src", "main", "java", "com", "houzicore", "arcade",
                "nautilus", "game", "arcade", "game", "games", "primalgames", "SoloPrimalGames.java");

        assertTrue(primalGamesLang.contains("primal_games.announce.header"));
        assertTrue(lootTableManager.contains("primal_games.item.compass_name"));
        assertTrue(lootTableManager.contains("primal_games.item.compass_uses"));
        assertTrue(wanderingTraderManager.contains("primal_games.trader.menu_title"));
        assertTrue(wanderingTraderManager.contains("primal_games.trader.name"));
        assertTrue(soloPrimalGames.contains("primal_games.scoreboard.phase_survive"));
        assertTrue(soloPrimalGames.contains("primal_games.scoreboard.phase_border_closing"));
        assertTrue(soloPrimalGames.contains("primal_games.scoreboard.phase_deathmatch"));
        assertTrue(soloPrimalGames.contains("primal_games.scoreboard.top_killers"));
        assertTrue(soloPrimalGames.contains("primal_games.scoreboard.no_kills"));
    }

    private static String read(String... parts) throws IOException
    {
        return Files.readString(Path.of(parts[0], java.util.Arrays.copyOfRange(parts, 1, parts.length)),
                StandardCharsets.UTF_8);
    }
}
