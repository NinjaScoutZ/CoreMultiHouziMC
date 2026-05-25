package com.houzicore.arcade.bootstrap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;
import com.houzicore.shared.api.context.PlayerContextId;
import com.houzicore.shared.api.context.TransitionReason;
import com.houzicore.arcade.nautilus.game.arcade.managers.GameStatManager;

public class ArcadeTransitionCoordinator implements Listener {

    private final ArcadeManager arcadeManager;

    public ArcadeTransitionCoordinator(ArcadeManager arcadeManager) {
        this.arcadeManager = arcadeManager;
        arcadeManager.getPluginManager().registerEvents(this, arcadeManager.getPlugin());
    }

    /**
     * Handles contextual transitions based on the overall game state.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameStateChange(GameStateChangeEvent event) {
        Game game = event.GetGame();
        if (!game.isContextRuntime()) {
            return; // Fallback rule for legacy games
        }

        ArcadeBootstrap bootstrap = ArcadeBootstrap.getInstance();
        GameState state = event.GetState();
        PlayerContextId targetContext = null;
        TransitionReason reason = null;
        boolean applyImmediately = true;

        if (state == GameState.Prepare) {
            targetContext = PlayerContextId.ARCADE_PREP;
            reason = TransitionReason.GAME_START;
            // Prep state is re-applied after each player is teleported into the arena.
            applyImmediately = false;
        } else if (state == GameState.Live) {
            targetContext = PlayerContextId.ARCADE_LIVE;
            reason = TransitionReason.GAME_START;
        } else if (state == GameState.End) {
            targetContext = PlayerContextId.ARCADE_POSTGAME;
            reason = TransitionReason.GAME_END;
        }

        if (targetContext != null) {
            for (Player player : game.GetPlayers(true)) {
                if (targetContext == PlayerContextId.ARCADE_PREP &&
                        bootstrap.getContextService().getCurrentContextId(player) == PlayerContextId.ARCADE_LOBBY) {
                    bootstrap.captureRoundSnapshot(player);
                }
                bootstrap.transition(player, targetContext, reason);
                if (applyImmediately) {
                    bootstrap.applyCurrentContextState(player);
                }
            }
        }
    }

    /**
     * Handles the exact moment a player dies. We push them into ARCADE_DEAD immediately.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Game game = arcadeManager.GetGame();
        if (game == null || game.GetState() != GameState.Live || !game.isContextRuntime()) {
            return;
        }

        Player player = event.getEntity();
        if (!game.IsAlive(player)) { // Player is definitively out
            ArcadeBootstrap.getInstance().transition(player, PlayerContextId.ARCADE_DEAD, TransitionReason.PLAYER_DEATH);
        }
    }

    public void transitionToSpectator(Player player, TransitionReason reason) {
        ArcadeBootstrap.getInstance().transitionAndApply(player, PlayerContextId.ARCADE_SPECTATOR, reason);
    }

    /**
     * Canonical pipeline for player exit / disconnect handling.
     * Replaces disparate listeners scattered across Game, GameStatManager, and Bootstrap.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        handlePlayerExit(event.getPlayer(), "QUIT");
    }

    public void handlePlayerExit(Player player, String reason) {
        ArcadeBootstrap bootstrap = ArcadeBootstrap.getInstance();
        if (bootstrap != null) {
            // 1. Snapshot and Context state cleanup run first
            bootstrap.getContextService().cleanup(player.getUniqueId());
            bootstrap.getSnapshotService().cleanup(player.getUniqueId());
        }

        // 2. Game Level Cleanup (Scoreboard, _playerKit, _gemCount flush)
        if (arcadeManager.GetGame() != null) {
            arcadeManager.GetGame().cleanupPlayer(player);
        }

        // 3. Commit/Flush statistics
        if (arcadeManager.getGameStatManager() != null) {
            arcadeManager.getGameStatManager().flushExitStats(player);
        }
    }
}
