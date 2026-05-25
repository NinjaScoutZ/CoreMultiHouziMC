package com.houzicore.arcade.nautilus.game.arcade.managers;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Particle;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

/**
 * Arrow Trail Effects (#37) — particles follow arrows in flight.
 * Effect is set per-shooter based on their cosmetic selection.
 */
public class ArrowTrailManager implements Listener {

    public enum ArrowTrail {
        NONE,
        FLAME,      // orange flame trail
        ICE,        // white snowflake trail
        VOID,       // portal/void trail
        RAINBOW,    // cycling-color redstone
        HEARTS      // hearts trail
    }

    private final ArcadeManager Manager;
    // Track active arrows → their effect type
    private final HashMap<UUID, ArrowTrail> _arrowTrails = new HashMap<>();

    public ArrowTrailManager(ArcadeManager manager) {
        Manager = manager;
        Manager.getPluginManager().registerEvents(this, Manager.getPlugin());
    }

    @EventHandler
    public void onArrowLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player shooter)) return;

        ArrowTrail trail = getArrowTrail(shooter);
        if (trail != ArrowTrail.NONE) {
            _arrowTrails.put(arrow.getUniqueId(), trail);
        }
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        _arrowTrails.remove(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Will self-clean on next tick when arrows expire
    }

    @EventHandler
    public void onTick(UpdateEvent event) {
        if (event.getType() != UpdateType.TICK) return;

        _arrowTrails.entrySet().removeIf(entry -> {
            org.bukkit.entity.Entity ent = org.bukkit.Bukkit.getEntity(entry.getKey());
            if (ent == null || ent.isDead() || !ent.isValid()) return true;

            Location loc = ent.getLocation();
            spawnTrail(loc, entry.getValue());
            return false;
        });
    }

    private void spawnTrail(Location loc, ArrowTrail trail) {
        switch (trail) {
            case FLAME -> loc.getWorld().spawnParticle(Particle.FLAME, loc, 3, 0.05, 0.05, 0.05, 0.0);
            case ICE   -> loc.getWorld().spawnParticle(Particle.SNOWFLAKE, loc, 4, 0.05, 0.05, 0.05, 0.0);
            case VOID  -> loc.getWorld().spawnParticle(Particle.PORTAL, loc, 6, 0.1, 0.1, 0.1, 0.1);
            case RAINBOW -> {
                // Cycle through colors using tick time
                int hue = (int)(System.currentTimeMillis() / 20) % 360;
                java.awt.Color jColor = java.awt.Color.getHSBColor(hue / 360f, 1f, 1f);
                org.bukkit.Color bColor = org.bukkit.Color.fromRGB(jColor.getRed(), jColor.getGreen(), jColor.getBlue());
                Particle.DustOptions dust = new Particle.DustOptions(bColor, 1.2f);
                loc.getWorld().spawnParticle(Particle.DUST, loc, 4, 0.05, 0.05, 0.05, dust);
            }
            case HEARTS -> loc.getWorld().spawnParticle(Particle.HEART, loc, 2, 0.1, 0.1, 0.1, 0.05);
            default -> {}
        }
    }

    private ArrowTrail getArrowTrail(Player player) {
        // TODO: load from DonationManager/preferences. Default FLAME.
        return ArrowTrail.FLAME;
    }
}
