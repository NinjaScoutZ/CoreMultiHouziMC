package com.houzicore.arcade.nautilus.game.arcade.game.modules;

import org.bukkit.event.EventHandler;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

public class SpawnShieldModule extends GameModule<Game> {
    private final long _duration;

    public SpawnShieldModule(Game game, long durationMs) {
        super(game);
        _duration = durationMs;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!isActive() || _game.GetState() != Game.GameState.Live) return;
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (!_game.IsPlaying(player)) return;

        long liveTime = System.currentTimeMillis() - _game.getGameLiveTime();
        if (liveTime < _duration) {
            event.setCancelled(true);
            // Visual block protection could be handled here with particles
        }
    }
}
