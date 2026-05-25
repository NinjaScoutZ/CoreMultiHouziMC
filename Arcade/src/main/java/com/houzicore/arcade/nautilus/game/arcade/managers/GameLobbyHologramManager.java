package com.houzicore.arcade.nautilus.game.arcade.managers;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.hologram.Hologram;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameLobbyHologramManager implements Listener {

    private ArcadeManager Manager;
    private Hologram _leaderboardHologram;

    public GameLobbyHologramManager(ArcadeManager manager) {
        Manager = manager;
        Manager.getPluginManager().registerEvents(this, Manager.getPlugin());
    }

    @EventHandler
    public void onGameStateChange(GameStateChangeEvent event) {
        if (event.GetState() == GameState.Recruit) {
            spawnLeaderboard();
        } else if (event.GetState() == GameState.Prepare || event.GetState() == GameState.Live || event.GetState() == GameState.Dead) {
            removeLeaderboard();
        }
    }

    private void spawnLeaderboard() {
        removeLeaderboard(); // Ensure no duplicates

        if (Manager.GetGame() == null) return;
        
        String gameName = Manager.GetGame().GetName();
        Location loc = Manager.GetLobby().GetSpawn().clone();
        loc.setX(0.5);
        loc.setZ(0.5);
        loc.setY(107.5); // Floating above spawn

        // Example: pulling wins for the current game from the StatsManager
        // In HouziCore, stats are stored like "GameName.Wins".
        // However, retrieving all-time top 5 requires a leaderboard query if available in StatsManager.
        // For now, we will display a placeholder or simple text, as fully syncing global Top 5 requires a DB query callback.
        
        Manager.GetStatsManager().getTopStatsAsync(gameName + ".Wins", 5, top -> {
            List<String> lines = new ArrayList<>();
            lines.add(C.cGold + C.Bold + "🏆 Top Players - " + gameName + " 🏆");
            lines.add("");

            if (top == null || top.isEmpty()) {
                lines.add(C.cGray + "No stats found...");
            } else {
                int rank = 1;
                for (Map.Entry<String, Long> entry : top.entrySet()) {
                    lines.add(C.cGreen + rank + ". " + C.cYellow + entry.getKey() + C.cGray + " - " + C.cWhite + entry.getValue());
                    rank++;
                }
            }

            String[] textArray = lines.toArray(new String[0]);
            
            // Note: Make sure to teleport the text appropriately if it needs adjustments
            loc.setY(107.5 + (lines.size() * 0.25)); 
            
            _leaderboardHologram = new Hologram(Manager.getHologramManager(), loc, textArray);
            _leaderboardHologram.start();
        });
    }

    private void removeLeaderboard() {
        if (_leaderboardHologram != null) {
            _leaderboardHologram.stop();
            _leaderboardHologram = null;
        }
    }
}
