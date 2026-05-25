package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.gadget.CosmeticRarity;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.ParticleGadget;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

/**
 * Phoenix Wings — Premium particle wings that persist in Arcade minigames.
 * Uses DUST_COLOR_TRANSITION for smooth fire-colored wings (orange→red→gold).
 * Flaps dynamically with speed and emits ember trails when moving.
 */
public class ParticlePhoenixWings extends ParticleGadget {

    // ── Fire palette (pairs for DUST_COLOR_TRANSITION) ──
    private static final Color FIRE_CORE   = Color.fromRGB(255, 200, 0);   // Bright Gold
    private static final Color FIRE_EDGE   = Color.fromRGB(255, 60, 0);    // Deep Red-Orange
    private static final Color EMBER_FROM  = Color.fromRGB(255, 120, 0);   // Orange
    private static final Color EMBER_TO    = Color.fromRGB(255, 220, 80);  // Pale Gold
    private static final Color TRAIL_FROM  = Color.fromRGB(200, 40, 0);    // Dark Ember
    private static final Color TRAIL_TO    = Color.fromRGB(80, 0, 0);      // Ash

    public ParticlePhoenixWings(GadgetManager manager) {
        super(manager, "Phoenix Wings",
                new String[] {
                    C.cWhite + "Reborn from the ashes,",
                    C.cWhite + "these fiery wings follow",
                    C.cWhite + "you into battle.",
                    " ",
                    C.cGold + "✦ " + C.cYellow + "Works in Minigames!"
                }, -2, Material.BLAZE_POWDER, (byte) 0);
    }

    @Override
    public CosmeticRarity getRarity() {
        return CosmeticRarity.MYTHIC;
    }

    @Override
    public boolean isGameCompatible() {
        return true;
    }

    @EventHandler
    public void playParticle(UpdateEvent event) {
        if (event.getType() != UpdateType.TICK) {
            return;
        }

        for (Player player : GetActive()) {
            if (!shouldDisplay(player)) {
                continue;
            }

            drawWings(player);

            // Ember trail when moving
            if (Manager.isMoving(player)) {
                drawEmberTrail(player);
            }
        }
    }

    private void drawWings(Player player) {
        Vector forward = player.getLocation().getDirection().setY(0).normalize();
        if (forward.lengthSquared() == 0) {
            forward = new Vector(0, 0, 1);
        }
        Vector side = new Vector(-forward.getZ(), 0, forward.getX()).normalize();

        // Timing — each player gets a unique offset so multiple phoenixes don't sync
        double timeOffset = (player.getUniqueId().hashCode() & 0xFFFF) / 1000.0;
        double time = (System.currentTimeMillis() / 1000.0) + timeOffset;

        // Dynamic flap speed: sprint=fast, sneak=slow, walk=medium
        double flapSpeed = player.isSprinting() ? 12.0 : (player.isSneaking() ? 3.5 : 7.0);
        double flapAngle = Math.toRadians(30) * Math.sin(time * flapSpeed);
        double bob = Math.sin(time * flapSpeed * 0.5) * 0.04;

        var base = player.getLocation().clone()
                .add(0, 1.35 + bob, 0)
                .subtract(forward.clone().multiply(0.25));

        for (int wing = -1; wing <= 1; wing += 2) {
            double currentFlapAngle = flapAngle * wing;

            // Main feather points (10 points per wing for high-fidelity shape)
            for (int i = 0; i < 10; i++) {
                double progress = i / 9.0;

                // Wing shape: curved arc that sweeps back
                double spread = 0.2 + (progress * 1.4);          // Width
                double rise = 0.05 + Math.sin(progress * Math.PI) * 0.85; // Height arc
                double back = 0.03 + (progress * 0.32);          // Sweep back
                double featherCurve = Math.cos(progress * Math.PI) * 0.06;

                Vector offset = side.clone().multiply((spread + featherCurve) * wing)
                        .add(new Vector(0, rise, 0))
                        .subtract(forward.clone().multiply(back));

                offset.rotateAroundAxis(forward, currentFlapAngle);

                var point = base.clone().add(offset);

                // Inner feathers = bright gold core, outer feathers = red-orange edge
                Color from, to;
                float size;
                if (progress < 0.4) {
                    from = FIRE_CORE;
                    to = EMBER_FROM;
                    size = 1.1f;
                } else if (progress < 0.75) {
                    from = EMBER_FROM;
                    to = FIRE_EDGE;
                    size = 0.9f;
                } else {
                    from = FIRE_EDGE;
                    to = TRAIL_FROM;
                    size = 0.7f;
                }

                // Main wing particle — smooth color transition
                Particle.DustTransition wingDust = new Particle.DustTransition(from, to, size);
                point.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION,
                        point, 1, 0.01, 0.01, 0.01, 0, wingDust);

                // Every other point gets an extra glow particle for richness
                if (i % 2 == 0) {
                    point.getWorld().spawnParticle(Particle.FLAME,
                            point, 1, 0.01, 0.01, 0.01, 0.001);
                }
            }

            // Secondary feather row (shorter, creates depth)
            for (int i = 1; i < 7; i++) {
                double progress = i / 6.0;
                double spread = 0.15 + (progress * 0.9);
                double rise = -0.1 + Math.sin(progress * Math.PI) * 0.5;
                double back = 0.08 + (progress * 0.25);

                Vector offset = side.clone().multiply((spread) * wing)
                        .add(new Vector(0, rise, 0))
                        .subtract(forward.clone().multiply(back));

                offset.rotateAroundAxis(forward, currentFlapAngle);

                var point = base.clone().add(offset);

                Particle.DustTransition lowerDust = new Particle.DustTransition(EMBER_FROM, TRAIL_FROM, 0.6f);
                point.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION,
                        point, 1, 0.005, 0.005, 0.005, 0, lowerDust);
            }
        }
    }

    private void drawEmberTrail(Player player) {
        // Falling embers behind the player when moving
        var trailLoc = player.getLocation().clone().add(0, 1.2, 0)
                .subtract(player.getLocation().getDirection().setY(0).normalize().multiply(0.5));

        Particle.DustTransition emberDust = new Particle.DustTransition(EMBER_TO, TRAIL_TO, 0.5f);
        trailLoc.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION,
                trailLoc, 3, 0.3, 0.2, 0.3, 0, emberDust);

        // Occasional small flame puff
        if (player.getTicksLived() % 4 == 0) {
            trailLoc.getWorld().spawnParticle(Particle.SMALL_FLAME,
                    trailLoc, 1, 0.15, 0.1, 0.15, 0.005);
        }
    }

    @Override
    public void EnableCustom(Player player) {
        super.EnableCustom(player);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 0.6f, 1.5f);
    }
}
