package com.houzicore.arcade.nautilus.game.arcade.managers;

import java.util.ArrayList;
import java.util.HashSet;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilText;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import java.time.Duration;

/**
 * Tracks consecutive wins (Win Streaks) per-player.
 * Stored via StatsManager as "Global.WinStreak".
 */
public class WinStreakManager implements org.bukkit.event.Listener {

    private static final String WIN_STREAK_STAT = "Global.WinStreak";
    private final ArcadeManager Manager;

    public WinStreakManager(ArcadeManager manager) {
        Manager = manager;
        Manager.getPluginManager().registerEvents(this, Manager.getPlugin());
        
        com.houzicore.shared.core.reward.math.MultiplierEngine.registerProvider(new com.houzicore.shared.core.reward.math.MultiplierEngine.MultiplierProvider() {
            @Override
            public String getName() { return "Win Streak"; }

            @Override
            public double getBonus(org.bukkit.entity.Player player) {
                if (Manager.GetStatsManager().Get(player) == null) return 0.0;
                long winStreak = Manager.GetStatsManager().Get(player).getStat(WIN_STREAK_STAT);
                if (winStreak >= 3) return winStreak * 0.1; // +10% Essence per win streak!
                return 0.0;
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameEnd(GameStateChangeEvent event) {
        if (event.GetState() != GameState.Dead) return;

        Game game = event.GetGame();
        if (game == null) return;

        // "winners" = players still alive at game-end
        ArrayList<Player> winners = game.GetPlayers(true);

        HashSet<String> winnerNames = new HashSet<>();
        for (Player w : winners) {
            winnerNames.add(w.getName());
        }

        // Process streaks for all in-game players
        for (Player player : UtilServer.getPlayers()) {
            if (Manager.GetStatsManager() == null) continue;
            if (Manager.GetStatsManager().Get(player) == null) continue;

            boolean won = winnerNames.contains(player.getName());

            if (won) {
                Manager.GetStatsManager().incrementStat(player, WIN_STREAK_STAT, 1);
                long newStreak = Manager.GetStatsManager().Get(player).getStat(WIN_STREAK_STAT);
                if (newStreak >= 5) {
                    broadcastWinStreak(player, newStreak, game);
                }
            } else {
                long oldStreak = Manager.GetStatsManager().Get(player).getStat(WIN_STREAK_STAT);
                if (oldStreak > 0) {
                    Manager.GetStatsManager().incrementStat(player, WIN_STREAK_STAT, -oldStreak);
                }
            }
        }
    }

    private void broadcastWinStreak(Player player, long streak, Game game) {
        String color = streak >= 10 ? C.cGold : C.cYellow;
        game.Announce(F.main("Win Streak",
            color + player.getName() + C.cGray + " is on a " +
            C.cGreen + C.Bold + streak + C.cGray + "-win Streak!"));

        player.sendTitle(
            C.cGold + C.Bold + UtilText.toSmallCaps(streak + " win streak!"),
            C.cGray + "Keep it up!",
            8, 40, 12
        );
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
    }
}
