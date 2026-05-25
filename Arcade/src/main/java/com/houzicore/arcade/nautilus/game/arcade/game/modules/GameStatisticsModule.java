package com.houzicore.arcade.nautilus.game.arcade.game.modules;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;

import com.houzicore.arcade.nautilus.game.arcade.game.Game;

public class GameStatisticsModule extends GameModule<Game> {

    public GameStatisticsModule(Game game) {
        super(game);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        if (!isActive() || _game.GetState() != Game.GameState.Live) return;

        Player killed = event.getEntity();
        if (killed != null) {
            _game.AddStat(killed, "Death", 1, false, false);
        }

        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            _game.AddStat(killer, "Kill", 1, false, false);
        }
    }
}
