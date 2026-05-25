package com.houzicore.arcade.nautilus.game.arcade.game.games.skywars.data;

import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.arcade.nautilus.game.arcade.game.games.skywars.Skywars;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * MeteorShowerEvent — fireballs rain from the sky for 20 seconds.
 *
 * Each fireball:
 *   1. Projects a SHADOW INDICATOR (red ring on ground) 1 second before impact.
 *   2. Falls straight down.
 *   3. On hit: 2x2 block destruction + 2-3 heart damage + strong knockback.
 *   4. Max 12 fireballs active simultaneously (performance cap).
 */
public class MeteorShowerEvent
{
    private final Skywars _host;
    private final JavaPlugin _plugin;

    private boolean _active = false;
    private long _startTime;
    private long _lastSpawnTick = 0;

    private static final long DURATION_MS     = 20_000L; // 20 seconds
    private static final int  SPAWN_INTERVAL  = 10;      // ticks between spawns (0.5s)
    private static final int  MAX_FIREBALLS   = 12;
    private static final float DAMAGE          = 4.0f;   // 2 hearts
    private static final float KNOCKBACK       = 1.5f;

    // Track active fireballs so we can cap them
    private final List<Fireball> _activeFireballs = new ArrayList<>();

    private long _tick = 0;

    public void start()
    {
        _active    = true;
        _startTime = System.currentTimeMillis();
        _tick      = 0;
    }

    public MeteorShowerEvent(Skywars host, JavaPlugin plugin)
    {
        _host   = host;
        _plugin = plugin;
    }

    public boolean isActive() { return _active; }

    public void tick()
    {
        if (!_active) return;

        _tick++;

        if (System.currentTimeMillis() - _startTime >= DURATION_MS)
        {
            _active = false;
            return;
        }

        // Remove dead fireballs from tracking list
        _activeFireballs.removeIf(f -> !f.isValid());

        // Spawn a new fireball every SPAWN_INTERVAL ticks, if under cap
        if (_tick % SPAWN_INTERVAL == 0 && _activeFireballs.size() < MAX_FIREBALLS)
        {
            spawnMeteor();
        }
    }

    private void spawnMeteor()
    {
        // Pick a random X/Z within the map bounds
        int mapHalfWidth = (int)(((_host.WorldData.MaxX - _host.WorldData.MinX) / 2.0) * 0.8);
        int centerX      = (_host.WorldData.MaxX + _host.WorldData.MinX) / 2;
        int centerZ      = (_host.WorldData.MaxZ + _host.WorldData.MinZ) / 2;

        int rx = centerX + UtilMath.r(mapHalfWidth * 2) - mapHalfWidth;
        int rz = centerZ + UtilMath.r(mapHalfWidth * 2) - mapHalfWidth;

        // Find the highest block at this column
        int topY = _host.WorldData.World.getHighestBlockYAt(rx, rz);
        int spawnY = topY + 40;

        Location impactLoc = new Location(_host.WorldData.World, rx + 0.5, topY + 1, rz + 0.5);
        Location spawnLoc  = new Location(_host.WorldData.World, rx + 0.5, spawnY, rz + 0.5);

        // ── Shadow Indicator (1 second before impact) ──────────────
        drawShadowRing(impactLoc);

        // ── Spawn Fireball after 1 second ──────────────────────────
        new BukkitRunnable()
        {
            @Override public void run()
            {
                if (!_host.IsLive() || !_active) return;

                // Safety: don't spawn within 5 blocks of any player (fairness)
                for (Player p : _host.GetPlayers(true))
                {
                    if (_host.IsAlive(p) && p.getLocation().distance(impactLoc) < 5)
                        return;
                }

                _host.CreatureAllowOverride = true;
                Fireball fb = _host.WorldData.World.spawn(spawnLoc, Fireball.class);
                _host.CreatureAllowOverride = false;

                fb.setDirection(new Vector(0, -1, 0));
                fb.setYield(0f);         // disable vanilla explosion
                fb.setIsIncendiary(false);
                fb.setVelocity(new Vector(0, -1.2, 0));

                _activeFireballs.add(fb);

                // Schedule hit-detection (time it takes to fall spawnY→topY at v=1.2/tick)
                long fallTicks = Math.max(10L, (long)((spawnY - topY) / 1.2));

                new BukkitRunnable()
                {
                    @Override public void run()
                    {
                        if (!fb.isValid()) return;
                        detonateMeteor(fb, impactLoc);
                    }
                }.runTaskLater(_plugin, fallTicks);

                // Particle trail while falling
                new BukkitRunnable()
                {
                    @Override public void run()
                    {
                        if (!fb.isValid()) { cancel(); return; }
                        fb.getWorld().spawnParticle(Particle.FLAME,
                            fb.getLocation(), 4, 0.1, 0.1, 0.1, 0.05);
                        fb.getWorld().spawnParticle(Particle.LAVA,
                            fb.getLocation(), 2, 0.1, 0.1, 0.1, 0);
                    }
                }.runTaskTimer(_plugin, 0L, 1L);
            }
        }.runTaskLater(_plugin, 20L); // 1s shadow warning
    }

    /** Draw a ring of red dust particles on the ground as warning. */
    private void drawShadowRing(Location center)
    {
        new BukkitRunnable()
        {
            int _steps = 0;
            @Override public void run()
            {
                if (_steps++ > 20) { cancel(); return; } // 1 second

                Particle.DustOptions dust = new Particle.DustOptions(org.bukkit.Color.RED, 1.0f);
                for (int i = 0; i < 16; i++)
                {
                    double angle = (2 * Math.PI / 16) * i;
                    double rx = center.getX() + Math.cos(angle) * 2.0;
                    double rz = center.getZ() + Math.sin(angle) * 2.0;
                    center.getWorld().spawnParticle(Particle.DUST,
                        new Location(center.getWorld(), rx, center.getY() + 0.1, rz),
                        1, 0, 0, 0, 0, dust);
                }
            }
        }.runTaskTimer(_plugin, 0L, 1L);
    }

    /** Custom explosion on impact — block damage + player damage/knockback. */
    private void detonateMeteor(Fireball fb, Location impact)
    {
        if (fb.isValid()) fb.remove();

        // Sound + particles
        impact.getWorld().spawnParticle(Particle.EXPLOSION, impact, 3, 0.5, 0.5, 0.5, 0);
        impact.getWorld().spawnParticle(Particle.FLAME, impact, 20, 0.5, 0.5, 0.5, 0.1);
        impact.getWorld().playSound(impact, Sound.ENTITY_GENERIC_EXPLODE, 2.0f, 0.8f);

        // Destroy 2×2 area around impact (skip chest, bell, ore nodes)
        for (int dx = -1; dx <= 1; dx++)
        {
            for (int dz = -1; dz <= 1; dz++)
            {
                Block b = impact.getBlock().getRelative(dx, 0, dz);
                if (isSafeBlock(b.getType())) continue;
                b.breakNaturally();
            }
        }

        // Player damage + knockback
        for (Player p : _host.GetPlayers(true))
        {
            if (!_host.IsAlive(p)) continue;
            double dist = p.getLocation().distance(impact);
            if (dist > 5) continue;

            // Knockback direction away from impact
            Vector kb = p.getLocation().toVector()
                .subtract(impact.toVector())
                .normalize()
                .multiply(KNOCKBACK)
                .setY(0.7);
            p.setVelocity(kb);

            // Damage scales with distance (full damage within 2 blocks)
            double dmgFraction = Math.max(0, 1 - (dist / 5.0));
            p.damage(DAMAGE * dmgFraction);
        }
    }

    private boolean isSafeBlock(Material m)
    {
        return m == Material.AIR
            || m == Material.CHEST
            || m == Material.BELL
            || m.name().contains("ORE");
    }

    public void cleanUp()
    {
        _active = false;
        for (Fireball fb : _activeFireballs)
            if (fb.isValid()) fb.remove();
        _activeFireballs.clear();
    }
}
