package com.houzicore.arcade.nautilus.game.arcade.game.games.skywars.data;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTextMiddle;
import com.houzicore.arcade.nautilus.game.arcade.game.games.skywars.Skywars;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * DragonStrafingEvent — a real Ender Dragon flies straight across the map,
 * manually destroying blocks in a 4-block radius and knocking back players.
 *
 * AI is disabled; the dragon is moved by teleport each tick (straight line).
 * Block destruction uses manual radius check instead of vanilla AI for performance.
 */
public class DragonStrafingEvent
{
    private final Skywars    _host;
    private final JavaPlugin _plugin;

    private boolean     _active = false;
    private EnderDragon _dragon;
    private BossBar     _warningBar;

    // Flight state
    private Location _currentPos;
    private Vector   _flightDir;
    private Location _endPos;
    private float    _speed = 1.2f; // blocks per tick

    private static final int DESTROY_RADIUS = 4;
    private static final int KNOCKBACK_RADIUS = 6;
    private static final double PLAYER_DAMAGE = 4.0; // 2 hearts

    private long _tick = 0;

    // Warning phase fields
    private boolean _warningStarted = false;
    private boolean _dragonSpawned  = false;

    public DragonStrafingEvent(Skywars host, JavaPlugin plugin)
    {
        _host   = host;
        _plugin = plugin;
    }

    public void start()
    {
        _active = true;
        showWarning();
    }

    /** Show 10-second warning BossBar + sound, then spawn dragon. */
    private void showWarning()
    {
        _warningBar = BossBar.bossBar(
            Component.text("🐉 DRAGON INCOMING — 10s").color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD),
            1.0f,
            BossBar.Color.PURPLE,
            BossBar.Overlay.PROGRESS
        );
        for (Player p : UtilServer.getPlayers())
        {
            p.showBossBar(_warningBar);
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 3.0f, 0.7f);
        }

        for (Player p : UtilServer.getPlayers())
        {
            boolean th = p != null && "THA".equals(com.houzicore.shared.core.lang.LangManager.get().resolveLocaleStr(p));
            p.sendMessage(com.houzicore.shared.common.util.F.main("Game", th 
                ? "§d§l🐉 มังกรเอ็นเดอร์กำลังบินผ่านแผนที่! ระวังบล็อกพังและแรงผลัก!" 
                : "§d§l🐉 An Ender Dragon is strafing the map! Watch out for block destruction!"));
        }

        // Countdown the BossBar
        new BukkitRunnable()
        {
            int _countdown = 10;
            @Override public void run()
            {
                if (!_host.IsLive() || !_active) { cancel(); return; }

                _countdown--;
                float p = (float) _countdown / 10f;
                _warningBar.progress(Math.max(0f, p));
                _warningBar.name(Component.text("🐉 DRAGON INCOMING — " + _countdown + "s")
                    .color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD));

                if (_countdown <= 0)
                {
                    hideWarningBar();
                    spawnAndFly();
                    cancel();
                }
            }
        }.runTaskTimer(_plugin, 20L, 20L);
    }

    private void hideWarningBar()
    {
        if (_warningBar != null)
        {
            for (Player p : UtilServer.getPlayers())
                p.hideBossBar(_warningBar);
            _warningBar = null;
        }
    }

    /** Calculate flight path and spawn the dragon. */
    private void spawnAndFly()
    {
        // Map center
        int centerX = (_host.WorldData.MaxX + _host.WorldData.MinX) / 2;
        int centerZ = (_host.WorldData.MaxZ + _host.WorldData.MinZ) / 2;

        // Average top Y of map
        int avgY = _host.WorldData.World.getHighestBlockYAt(centerX, centerZ) + 10;

        // Pick a random cardinal / diagonal direction
        int dir = (int)(Math.random() * 4);
        int halfW = (_host.WorldData.MaxX - _host.WorldData.MinX) / 2 + 20;
        int halfL = (_host.WorldData.MaxZ - _host.WorldData.MinZ) / 2 + 20;

        Location[] paths = {
            // N→S
            new Location(_host.WorldData.World, centerX, avgY, _host.WorldData.MinZ - 20),
            // S→N
            new Location(_host.WorldData.World, centerX, avgY, _host.WorldData.MaxZ + 20),
            // W→E
            new Location(_host.WorldData.World, _host.WorldData.MinX - 20, avgY, centerZ),
            // E→W
            new Location(_host.WorldData.World, _host.WorldData.MaxX + 20, avgY, centerZ),
        };
        Location[] ends = {
            new Location(_host.WorldData.World, centerX, avgY, _host.WorldData.MaxZ + 20),
            new Location(_host.WorldData.World, centerX, avgY, _host.WorldData.MinZ - 20),
            new Location(_host.WorldData.World, _host.WorldData.MaxX + 20, avgY, centerZ),
            new Location(_host.WorldData.World, _host.WorldData.MinX - 20, avgY, centerZ),
        };

        _currentPos = paths[dir].clone();
        _endPos     = ends[dir].clone();
        _flightDir  = _endPos.toVector()
            .subtract(_currentPos.toVector())
            .normalize()
            .multiply(_speed);

        // Spawn real EnderDragon
        _host.CreatureAllowOverride = true;
        _dragon = _host.WorldData.World.spawn(_currentPos, EnderDragon.class, d -> {
            d.setPhase(EnderDragon.Phase.LEAVE_PORTAL); // passive phase = no AI attack
        });
        _host.CreatureAllowOverride = false;

        if (_dragon != null)
        {
            _dragon.setAware(false); // disable AI movement
        }
    }

    public boolean isActive() { return _active; }

    public void tick()
    {
        if (!_active) return;
        if (_dragon == null) return;

        _tick++;

        if (!_dragon.isValid())
        {
            _active = false;
            return;
        }

        // Move dragon along straight-line path and set its facing direction
        _currentPos = _currentPos.clone().add(_flightDir);
        _currentPos.setDirection(_flightDir);
        _dragon.teleport(_currentPos);

        // Periodic sound
        if (_tick % 10 == 0)
        {
            for (Player p : _host.GetPlayers(true))
            {
                double dist = p.getLocation().distance(_currentPos);
                float vol   = (float) Math.max(0.3, 2.0 - dist / 30.0);
                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, vol, 0.9f);
            }
        }

        // Particle trail (dragon trail using safe portal/witch particles to avoid 1.21 Float data req)
        _dragon.getWorld().spawnParticle(Particle.PORTAL,
            _currentPos.clone().add(0, -2, 0), 30, 1.5, 0.5, 1.5, 0.02);
        _dragon.getWorld().spawnParticle(Particle.WITCH,
            _currentPos.clone().add(0, -2, 0), 10, 1.5, 0.5, 1.5, 0.02);

        // Manual block destruction — radius check (every 3 ticks for performance)
        if (_tick % 3 == 0)
        {
            destroyNearbyBlocks();
        }

        // Player damage/knockback
        damageNearbyPlayers();

        // Check if dragon has flown past the exit point
        if (_currentPos.distance(_endPos) < _speed * 2)
        {
            _dragon.remove();
            _active = false;

            for (Player p : UtilServer.getPlayers())
            {
                boolean th = p != null && "THA".equals(com.houzicore.shared.core.lang.LangManager.get().resolveLocaleStr(p));
                p.sendMessage(com.houzicore.shared.common.util.F.main("Game", th 
                    ? "§d§l🐉 มังกรเอ็นเดอร์บินจากไปแล้ว..." 
                    : "§d§l🐉 The Ender Dragon departs..."));
            }
        }
    }

    /** Destroy blocks in DESTROY_RADIUS around the dragon (safe blocks excluded). */
    private void destroyNearbyBlocks()
    {
        Location pos = _currentPos;
        for (int dx = -DESTROY_RADIUS; dx <= DESTROY_RADIUS; dx++)
        {
            for (int dy = -2; dy <= 2; dy++)
            {
                for (int dz = -DESTROY_RADIUS; dz <= DESTROY_RADIUS; dz++)
                {
                    if (dx*dx + dz*dz > DESTROY_RADIUS*DESTROY_RADIUS) continue;

                    Block b = pos.getWorld().getBlockAt(
                        pos.getBlockX() + dx,
                        pos.getBlockY() + dy,
                        pos.getBlockZ() + dz);

                    if (isSafeBlock(b.getType())) continue;

                    // Visual feedback before removing
                    if (Math.random() < 0.3)
                    {
                        b.getWorld().spawnParticle(Particle.BLOCK, b.getLocation().add(0.5, 0.5, 0.5),
                            5, 0.3, 0.3, 0.3, 0, b.getBlockData());
                    }
                    b.setType(Material.AIR);
                }
            }
        }
    }

    private void damageNearbyPlayers()
    {
        for (Player p : _host.GetPlayers(true))
        {
            if (!_host.IsAlive(p)) continue;
            double dist = p.getLocation().distance(_currentPos);
            if (dist > KNOCKBACK_RADIUS) continue;

            // Knockback away from dragon
            Vector kb = p.getLocation().toVector()
                .subtract(_currentPos.toVector())
                .normalize()
                .multiply(1.8)
                .setY(0.6);
            p.setVelocity(kb);

            // 2 hearts damage scaled by distance
            double dmgFrac = Math.max(0, 1.0 - (dist / KNOCKBACK_RADIUS));
            p.damage(PLAYER_DAMAGE * dmgFrac);

            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 1.5f, 1.0f);
        }
    }

    private boolean isSafeBlock(Material m)
    {
        return m == Material.AIR
            || m == Material.VOID_AIR
            || m == Material.CAVE_AIR
            || m == Material.CHEST
            || m == Material.BELL
            || m.name().contains("ORE")
            || m.name().contains("BEDROCK");
    }

    public void cleanUp()
    {
        _active = false;
        hideWarningBar();
        if (_dragon != null && _dragon.isValid())
            _dragon.remove();
    }
}
