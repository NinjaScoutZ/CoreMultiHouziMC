package com.houzicore.shared.core.gadget.gadgets.tracer;

import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.Gadget;
import com.houzicore.shared.core.gadget.types.GadgetType;
import com.houzicore.shared.core.gadget.types.TracerGadget;

/**
 * Standalone listener that hooks existing {@link TracerGadget} effects into
 * Arcade game combat.  When a player with an active Tracer gadget shoots a bow
 * or launches a projectile, this hook spawns the tracer's particle effect along
 * the projectile path using a repeating tick task.
 * <p>
 * Register this listener via {@link GadgetManager} to activate combat trails.
 */
public class TracerCombatHook implements Listener {

    private final GadgetManager _gadgetManager;

    public TracerCombatHook(GadgetManager gadgetManager) {
        _gadgetManager = gadgetManager;
    }

    // ── Event Handlers ─────────────────────────────────────────────

    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player shooter)) return;
        if (!(event.getProjectile() instanceof Projectile projectile)) return;

        handleTracer(shooter, projectile);
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player shooter)) return;

        handleTracer(shooter, event.getEntity());
    }

    // ── Core Logic ─────────────────────────────────────────────────

    private void handleTracer(Player shooter, Projectile projectile) {
        // Only apply in cosmetic-enabled contexts (lobby or active game world)
        if (!isCosmeticAllowed(shooter)) return;

        // Check if the player has an active Tracer gadget
        Gadget activeTracer = _gadgetManager.getActive(shooter, GadgetType.Tracer);
        if (activeTracer == null) return;

        // If the active gadget is a TracerGadget, delegate to its own playTracer method
        if (activeTracer instanceof TracerGadget tracerGadget) {
            tracerGadget.playTracer(projectile);
            return;
        }

        // Fallback: generic particle trail using the default CRIT particle
        spawnGenericTrail(projectile);
    }

    /**
     * Checks whether the player is in a world that allows cosmetics.
     * Players in the lobby or currently in an active Arcade game are allowed.
     */
    private boolean isCosmeticAllowed(Player player) {
        // Lobby worlds always allow cosmetics
        String worldName = player.getWorld().getName().toLowerCase();
        if (worldName.contains("lobby") || worldName.contains("hub")) {
            return true;
        }

        // In-game worlds: only allow if the player is actively in a game
        // (i.e. the GadgetManager hasn't suspended their cosmetics)
        Gadget active = _gadgetManager.getActive(player, GadgetType.Tracer);
        return active != null;
    }

    /**
     * Spawns a generic CRIT particle trail along the projectile's path as a
     * fallback when the active gadget does not provide its own effect.
     */
    private void spawnGenericTrail(Projectile projectile) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!projectile.isValid() || projectile.isOnGround()) {
                    this.cancel();
                    return;
                }

                projectile.getWorld().spawnParticle(
                        Particle.CRIT,
                        projectile.getLocation(),
                        1,   // count
                        0.0, // offsetX
                        0.0, // offsetY
                        0.0, // offsetZ
                        0.0  // extra (speed)
                );
            }
        }.runTaskTimer(_gadgetManager.getPlugin(), 0L, 1L);
    }
}
