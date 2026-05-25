package com.houzicore.arcade.nautilus.game.arcade.game.modules;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.houzicore.arcade.nautilus.game.arcade.game.Game;

public class DisableFallDamageModule extends GameModule<Game> {

    public DisableFallDamageModule(Game game) {
        super(game);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onFallDamage(EntityDamageEvent event) {
        if (!_active || _game.GetState() != Game.GameState.Live) return;

        if (event.getCause() == DamageCause.FALL) {
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                if (_game.IsAlive(player)) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
