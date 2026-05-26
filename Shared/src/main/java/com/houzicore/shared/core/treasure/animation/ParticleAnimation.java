package com.houzicore.shared.core.treasure.animation;

import org.bukkit.Particle;

import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.treasure.Treasure;
import com.houzicore.shared.core.treasure.TreasureStyle;
import com.houzicore.shared.core.treasure.TreasureType;

import org.bukkit.Location;

/**
 * Ambient orbit particle that runs continuously around the Treasure pedestal.
 *
 * Bug fix: the original class used a static pre-computed PATH ArrayList shared
 * across every instance, causing a race condition when multiple Treasure sessions
 * ran simultaneously.  This rewrite uses fully parametric per-tick math — no
 * shared state at all.
 *
 * Tier orbits:
 *   OLD      — gentle sinusoidal enchant-sparkle loop (radius 1.5, slow)
 *   ANCIENT  — rising flame helix (radius 2.0, medium, with FLAME secondary)
 *   MYTHICAL — triple-arm soul-fire + portal vortex (radius 2.5, fast)
 */
public class ParticleAnimation extends Animation {

    public ParticleAnimation(Treasure treasure) {
        super(treasure);
    }

    @Override
    protected void onFinish() { }

    @Override
    protected void tick() {
        int           t     = getTicks();
        TreasureType  tier  = getTreasure().getTreasureType();
        TreasureStyle style = tier.getStyle();
        Location      base  = getTreasure().getCenterBlock().getLocation().add(0.5, 0, 0.5);

        switch (tier) {
            case OLD: {
                // Gentle sinusoidal orbit — small enchant sparkles
                double angle = t * 0.15;
                double x = Math.cos(angle) * 1.5;
                double z = Math.sin(angle) * 1.5;
                double y = 1.2 + 0.3 * Math.sin(t * 0.3);
                UtilParticle.PlayParticle(style.getSecondaryParticle(),
                        base.clone().add(x, y, z), 0, 0, 0, 0, 1,
                        ViewDist.NORMAL, UtilServer.getPlayers());
                break;
            }
            case ANCIENT: {
                // Rising flame helix — medium speed
                double angle = t * 0.25;
                double x = Math.cos(angle) * 2.0;
                double z = Math.sin(angle) * 2.0;
                double y = 0.8 + ((t % 25) * 0.05); // gradual rise, resets every 25 ticks
                UtilParticle.PlayParticle(style.getSecondaryParticle(),
                        base.clone().add(x, y, z), 0.1F, 0.1F, 0.1F, 0, 1,
                        ViewDist.NORMAL, UtilServer.getPlayers());
                // Secondary FLAME trail slightly behind
                base.getWorld().spawnParticle(Particle.FLAME,
                        base.clone().add(x, y - 0.15, z), 1, 0, 0, 0, 0.02);
                break;
            }
            case MYTHICAL: {
                // Triple-arm soul-fire + portal vortex — fast
                double r = 2.5;
                for (int j = 0; j < 3; j++) {
                    double angle = t * 0.3 + (j * Math.PI * 2.0 / 3.0);
                    double x = Math.cos(angle) * r;
                    double z = Math.sin(angle) * r;
                    double y = 1.0 + Math.sin(t * 0.2 + j) * 0.4;
                    base.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME,
                            base.clone().add(x, y, z), 0, 0, 0, 0, 0);
                    base.getWorld().spawnParticle(Particle.PORTAL,
                            base.clone().add(x, y - 0.2, z), 1, 0, 0, 0, 0.1);
                }
                break;
            }
            case LEGENDARY: {
                // Double-helix golden star orbit — two intertwined spirals rising and falling
                double r = 2.2;
                for (int arm = 0; arm < 2; arm++) {
                    double angle = t * 0.2 + (arm * Math.PI);
                    double x = Math.cos(angle) * r;
                    double z = Math.sin(angle) * r;
                    double y = 1.0 + Math.sin(t * 0.12 + arm * Math.PI) * 0.5;
                    org.bukkit.Color from = org.bukkit.Color.fromRGB(255, 215, 0);  // Gold
                    org.bukkit.Color to   = org.bukkit.Color.fromRGB(255, 255, 150); // Pale Gold
                    Particle.DustTransition dust = new Particle.DustTransition(from, to, 1.0f);
                    base.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION,
                            base.clone().add(x, y, z), 1, 0.05, 0.05, 0.05, 0, dust);
                    // Subtle enchant sparkle trailing behind
                    base.getWorld().spawnParticle(Particle.ENCHANT,
                            base.clone().add(x, y - 0.1, z), 1, 0.05, 0.1, 0.05, 0.01);
                }
                break;
            }
            case IMMORTAL: {
                // Ice-blue / Cyan celestial orbit - rotating rings
                double r = 2.4;
                for (int j = 0; j < 2; j++) {
                    double angle = t * 0.15 + (j * Math.PI);
                    double x = Math.cos(angle) * r;
                    double z = Math.sin(angle) * r;
                    double y = 1.2 + Math.sin(t * 0.1 + j * Math.PI) * 0.4;
                    base.getWorld().spawnParticle(Particle.END_ROD, base.clone().add(x, y, z), 1, 0, 0, 0, 0);
                    org.bukkit.Color from = org.bukkit.Color.fromRGB(0, 198, 255);  // Ice Blue
                    org.bukkit.Color to   = org.bukkit.Color.fromRGB(0, 114, 255);  // Deep Blue
                    Particle.DustTransition dust = new Particle.DustTransition(from, to, 0.8f);
                    base.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION,
                            base.clone().add(x, y - 0.05, z), 1, 0.02, 0.02, 0.02, 0, dust);
                }
                break;
            }
            case DIVINE: {
                // Spectacular rainbow halo with runes and energy sparks
                double r = 2.6 + Math.sin(t * 0.07) * 0.3; // pulsating radius
                int points = 4;
                for (int j = 0; j < points; j++) {
                    double angle = t * 0.2 + (j * Math.PI * 2.0 / points);
                    double x = Math.cos(angle) * r;
                    double z = Math.sin(angle) * r;
                    double y = 1.4 + Math.cos(angle + t * 0.05) * 0.3;
                    base.getWorld().spawnParticle(Particle.ENCHANT, base.clone().add(x, y, z), 1, 0, 0, 0, 0.05);
                    base.getWorld().spawnParticle(Particle.TRIAL_SPAWNER_DETECTION, base.clone().add(x, y + 0.1, z), 1, 0, 0, 0, 0.02);
                }
                break;
            }
            case ILLUMINATED: {
                // Expanding cosmic ring — END_ROD particles in a tilted orbit
                double r = 2.5 + Math.sin(t * 0.05) * 0.5; // gently breathes in/out
                int points = 6;
                for (int j = 0; j < points; j++) {
                    double angle = t * 0.18 + (j * Math.PI * 2.0 / points);
                    double x = Math.cos(angle) * r;
                    double z = Math.sin(angle) * r;
                    // Tilted orbit: Y position depends on angle (creates a tilted ring)
                    double y = 1.3 + Math.sin(angle + t * 0.08) * 0.4;
                    base.getWorld().spawnParticle(Particle.END_ROD,
                            base.clone().add(x, y, z), 0, 0, 0, 0, 0);
                    // Purple–white dust shimmer
                    org.bukkit.Color from = org.bukkit.Color.fromRGB(170, 0, 255);  // Violet
                    org.bukkit.Color to   = org.bukkit.Color.fromRGB(255, 255, 255); // White
                    Particle.DustTransition dust = new Particle.DustTransition(from, to, 0.8f);
                    base.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION,
                            base.clone().add(x, y + 0.1, z), 1, 0, 0, 0, 0, dust);
                }
                break;
            }
            default: {
                // Fallback: simple circular orbit using secondary particle
                double angle = t * 0.2;
                UtilParticle.PlayParticle(style.getSecondaryParticle(),
                        base.clone().add(Math.cos(angle) * 1.5, 1.2, Math.sin(angle) * 1.5),
                        0, 0, 0, 0, 1, ViewDist.NORMAL, UtilServer.getPlayers());
                break;
            }
        }
    }
}
