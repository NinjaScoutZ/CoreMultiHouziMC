package com.houzicore.arcade.nautilus.game.arcade.game.modules;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;

import com.houzicore.arcade.nautilus.game.arcade.game.Game;

public class BloodModule extends GameModule<Game> {

    public BloodModule(Game game) {
        super(game);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!_active || _game.GetState() != Game.GameState.Live) return;

        if (!(event.getEntity() instanceof LivingEntity)) return;

        // Basic physics/combat damage causes blood (not void, fire, poison)
        switch (event.getCause()) {
            case ENTITY_ATTACK:
            case ENTITY_SWEEP_ATTACK:
            case PROJECTILE:
            case FALL:
            case BLOCK_EXPLOSION:
            case ENTITY_EXPLOSION:
            case CUSTOM:
                LivingEntity entity = (LivingEntity) event.getEntity();
                entity.getWorld().spawnParticle(
                        Particle.BLOCK, 
                        entity.getLocation().add(0, 1, 0), 
                        10, 0.3, 0.3, 0.3, 0.1, 
                        Material.REDSTONE_BLOCK.createBlockData()
                );
                break;
            default:
                break;
        }
    }
}
