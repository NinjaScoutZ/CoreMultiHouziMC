package com.houzicore.shared.common.util.effect;

import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Creates a cinematic explosion where blocks fly outward as FallingBlock entities.
 * Much more dramatic than standard particle-based explosions.
 *
 * <pre>
 * AnimatedExplosion.create(center, 5.0, 3.0)
 *     .withSound(Sound.ENTITY_GENERIC_EXPLODE)
 *     .withParticle(ParticleType.HUGE_EXPLOSION)
 *     .ignite(plugin);
 * </pre>
 *
 * Ported from:
 * - net.swofty.type.generic.utility.AnimatedExplosion
 * - net.swofty.type.generic.entity.ExplosionBlockEntity
 */
public class AnimatedExplosion implements Listener {

    private final Location center;
    private final double radius;
    private final double strength;
    private Sound sound = null;
    private ParticleType particle = null;

    // Fake explosion settings
    private boolean isFake = false;
    private Material fakeMaterial = Material.REDSTONE_BLOCK;
    private int fakeCount = 0;

    // Tracked entities for cleanup
    private final List<FallingBlock> debris = new ArrayList<>();
    private final long createdAt = System.currentTimeMillis();
    private static final long TIMEOUT_MS = 10_000; // 10 seconds
    private static final int MAX_DEBRIS = 40;

    private AnimatedExplosion(Location center, double radius, double strength) {
        this.center = center.clone();
        this.radius = radius;
        this.strength = strength;
    }

    private AnimatedExplosion(Location center, Material mat, int count, double strength) {
        this.center = center.clone();
        this.radius = 0; // Not used for fake
        this.strength = strength;
        this.isFake = true;
        this.fakeMaterial = mat;
        this.fakeCount = count;
    }

    /**
     * Factory method.
     * @param center   Center of the explosion
     * @param radius   Radius to collect blocks from (in blocks)
     * @param strength How hard blocks fly outward (recommended 2.0-5.0)
     */
    public static AnimatedExplosion create(Location center, double radius, double strength) {
        return new AnimatedExplosion(center, radius, strength);
    }

    /**
     * Factory method for creating an explosion of fake blocks (does not destroy real blocks).
     * Useful for player deaths or cinematic effects that shouldn't alter the map.
     */
    public static AnimatedExplosion createFake(Location center, Material mat, int count, double strength) {
        return new AnimatedExplosion(center, mat, count, strength);
    }

    /** Optional: play explosion sound */
    public AnimatedExplosion withSound(Sound sound) {
        this.sound = sound;
        return this;
    }

    /** Optional: spawn particle burst at center */
    public AnimatedExplosion withParticle(ParticleType particle) {
        this.particle = particle;
        return this;
    }

    /**
     * Execute the explosion. Call this once.
     * @param plugin JavaPlugin instance for registering the cleanup listener
     */
    public void ignite(JavaPlugin plugin) {
        World world = center.getWorld();
        if (world == null) return;

        // --- 1. Play sound ---
        if (sound != null) {
            world.playSound(center, sound, 1.0f, 1.0f);
        }

        // --- 2. Spawn particle burst ---
        if (particle != null) {
            UtilParticle.PlayParticle(particle, center, 0.5f, 0.5f, 0.5f, 0.1f, 10, ViewDist.LONG, com.houzicore.shared.common.util.UtilServer.getPlayers());
        }

        // --- 3. Determine debris blocks ---
        List<Location> spawnLocs = new ArrayList<>();
        List<Material> spawnMats = new ArrayList<>();

        if (isFake) {
            for (int i = 0; i < fakeCount; i++) {
                spawnLocs.add(center.clone().add(
                    (Math.random() - 0.5) * 1.5,
                    (Math.random() - 0.5) * 1.5 + 1.0,
                    (Math.random() - 0.5) * 1.5
                ));
                spawnMats.add(fakeMaterial);
            }
        } else {
            List<Block> blocks = new ArrayList<>();
            int r = (int) Math.ceil(radius);
            for (int x = -r; x <= r; x++) {
                for (int y = -r; y <= r; y++) {
                    for (int z = -r; z <= r; z++) {
                        Block block = world.getBlockAt(
                            center.getBlockX() + x,
                            center.getBlockY() + y,
                            center.getBlockZ() + z
                        );
                        if (block.getType() != Material.AIR
                            && block.getType() != Material.BEDROCK
                            && block.getType() != Material.BARRIER) {
                            double dist = block.getLocation().distance(center);
                            if (dist <= radius) {
                                blocks.add(block);
                            }
                        }
                    }
                }
            }

            // Limit to MAX_DEBRIS
            while (blocks.size() > MAX_DEBRIS) {
                blocks.remove(UtilMath.r(blocks.size()));
            }

            for (Block block : blocks) {
                spawnLocs.add(block.getLocation().add(0.5, 0.5, 0.5));
                spawnMats.add(block.getType());
                // Remove original block
                block.setType(Material.AIR);
            }
        }

        // --- 4. Spawn FallingBlock debris ---
        for (int i = 0; i < spawnLocs.size(); i++) {
            Location blockLoc = spawnLocs.get(i);
            Material mat = spawnMats.get(i);

            // Spawn FallingBlock
            FallingBlock fb = world.spawnFallingBlock(blockLoc, mat.createBlockData());
            fb.setDropItem(false);
            fb.setHurtEntities(false);

            // Calculate directional velocity
            double dx = blockLoc.getX() - center.getX();
            double dz = blockLoc.getZ() - center.getZ();

            // For fake blocks spawned tightly, give them a random outward spread if distance is 0
            if (isFake && dx == 0 && dz == 0) {
                dx = Math.random() - 0.5;
                dz = Math.random() - 0.5;
            }

            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist < 0.1) dist = 0.1;

            double vx = (dx / dist) * strength * (0.5 + Math.random() * 0.5);
            double vy = strength * 0.8 + Math.random() * 2.0;
            double vz = (dz / dist) * strength * (0.5 + Math.random() * 0.5);

            fb.setVelocity(new Vector(vx, vy, vz));
            debris.add(fb);
        }

        // --- 5. Register cleanup listener ---
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * UpdateEvent listener for cleanup.
     * Removes debris that has landed (velocity near-zero) or timed out.
     */
    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (event.getType() != UpdateType.FAST) return; // Check every 0.5 seconds

        boolean allDone = true;
        Iterator<FallingBlock> it = debris.iterator();
        while (it.hasNext()) {
            FallingBlock fb = it.next();

            // Entity invalid or dead
            if (!fb.isValid() || fb.isDead()) {
                it.remove();
                continue;
            }

            // Timeout check
            if (System.currentTimeMillis() - createdAt > TIMEOUT_MS) {
                fb.remove();
                it.remove();
                continue;
            }

            // Landing detection: velocity near-zero
            Vector vel = fb.getVelocity();
            if (Math.abs(vel.getX()) < 0.01 && Math.abs(vel.getZ()) < 0.01
                && fb.getTicksLived() > 10) {
                fb.remove();
                it.remove();
                continue;
            }

            allDone = false;
        }

        // All debris cleaned up — unregister this listener
        if (allDone || debris.isEmpty()) {
            HandlerList.unregisterAll(this);
        }
    }
}
