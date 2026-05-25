package com.houzicore.arcade.nautilus.game.arcade.game.games.skywars.data;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTextMiddle;
import com.houzicore.arcade.nautilus.game.arcade.game.games.skywars.Skywars;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * VoidRisingEvent — the Void rises 1 block every 2 seconds, replacing Map Crumble.
 *
 * Mechanics:
 *   - Players below voidY → Wither damage (2 hearts/s)
 *   - Players 3+ blocks below voidY → instant death (void kill)
 *   - Players 25+ blocks above voidY → Slowness I (anti-skybase)
 *   - Visual: purple dust ring around each player at void level
 *   - BossBar: shows current void level vs map top
 */
public class VoidRisingEvent
{
    private final Skywars    _host;
    private final JavaPlugin _plugin;

    private boolean _active = false;
    private long    _startTime;

    // Void rises 1 block every 2s
    private static final long RISE_INTERVAL_MS = 2_000L;
    private static final int  ANTI_CAMP_HEIGHT = 25;   // blocks above void → debuff

    private int  _voidLevel;
    private long _lastRiseTime;
    private long _lastDamageTick;
    private long _particleTick;

    private BossBar _voidBossBar;
    private int _mapTopY;

    public VoidRisingEvent(Skywars host, JavaPlugin plugin)
    {
        _host   = host;
        _plugin = plugin;
    }

    public void start()
    {
        _active       = true;
        _startTime    = System.currentTimeMillis();
        _voidLevel    = _host.WorldData.MinY;
        _lastRiseTime = System.currentTimeMillis();
        _lastDamageTick = System.currentTimeMillis();
        _particleTick = 0;

        // Calculate approx map top Y
        _mapTopY = _host.WorldData.MaxY;

        // Announce
        for (Player p : UtilServer.getPlayers())
        {
            boolean th = p != null && "THA".equals(com.houzicore.shared.core.lang.LangManager.get().resolveLocaleStr(p));
            p.sendMessage(com.houzicore.shared.common.util.F.main("Game", th 
                ? "§5§l🌑 ความมืดแห่งความว่างเปล่ากำลังคืบคลานขึ้นมา! จงขึ้นที่สูง!" 
                : "§5§l🌑 The Void is rising! Stay above the darkness!"));
        }

        for (Player p : UtilServer.getPlayers())
            p.playSound(p.getLocation(), Sound.ENTITY_WARDEN_ROAR, 3.0f, 0.8f);

        // Create BossBar
        _voidBossBar = BossBar.bossBar(
            Component.text("🌑 Void Level: ").color(NamedTextColor.DARK_PURPLE)
                .append(Component.text(_voidLevel + "").color(NamedTextColor.WHITE)),
            0.0f,
            BossBar.Color.PURPLE,
            BossBar.Overlay.PROGRESS
        );
        for (Player p : UtilServer.getPlayers())
            p.showBossBar(_voidBossBar);
    }

    public boolean isActive() { return _active; }

    public void tick()
    {
        if (!_active) return;

        long now = System.currentTimeMillis();
        _particleTick++;

        // ── Rise void level every RISE_INTERVAL ─────────────────────
        if (now - _lastRiseTime >= RISE_INTERVAL_MS)
        {
            _voidLevel++;
            _lastRiseTime = now;
            updateBossBar();
        }

        // ── Per-second damage and effects ────────────────────────────
        if (now - _lastDamageTick >= 1_000L)
        {
            _lastDamageTick = now;
            applyVoidEffects();
        }

        // ── Particle ring at void level (every 2 ticks) ──────────────
        if (_particleTick % 2 == 0)
        {
            drawVoidParticles();
        }
    }

    private void applyVoidEffects()
    {
        for (Player p : _host.GetPlayers(true))
        {
            if (!_host.IsAlive(p)) continue;

            double playerY = p.getLocation().getY();
            double diff    = playerY - _voidLevel;

            if (diff < -3)
            {
                // 3+ blocks below void → instant death
                p.damage(p.getHealth() + 1000);
                continue;
            }

            if (diff < 0)
            {
                // Below void level → Wither + sound
                p.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 40, 0, false, false), true);
                p.damage(2.0);
                if (_particleTick % 20 == 0)
                    p.playSound(p.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1.2f, 0.5f + (float)(Math.random() * 0.2));
            }
            else if (diff > ANTI_CAMP_HEIGHT)
            {
                // Anti-skybase: too high above void
                int heightBonus = (int)((diff - ANTI_CAMP_HEIGHT) / 10);
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, Math.min(2, heightBonus), false, false), true);
                p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 40, 0, false, false), true);

                // Shoot a void projectile up at the camper every 5 seconds
                if (_particleTick % 100 == 0)
                    shootVoidProjectile(p);
            }
        }
    }

    /** Shoot a slow-moving purple-tinted fireball upward toward a camper. */
    private void shootVoidProjectile(Player target)
    {
        // Source: directly below the player at void level
        org.bukkit.Location src = new org.bukkit.Location(
            target.getWorld(),
            target.getLocation().getX() + (Math.random() - 0.5) * 6,
            _voidLevel,
            target.getLocation().getZ() + (Math.random() - 0.5) * 6);

        Vector dir = target.getLocation().toVector()
            .subtract(src.toVector())
            .normalize()
            .multiply(0.6);

        _host.CreatureAllowOverride = true;
        org.bukkit.entity.Fireball fb = target.getWorld().spawn(src, org.bukkit.entity.Fireball.class);
        _host.CreatureAllowOverride = false;

        fb.setDirection(dir);
        fb.setYield(1.5f);
        fb.setIsIncendiary(false);
    }

    /** Draw a purple dust ring around each alive player at void level. */
    private void drawVoidParticles()
    {
        Particle.DustOptions dust = new Particle.DustOptions(org.bukkit.Color.fromRGB(80, 0, 160), 1.2f);

        for (Player p : _host.GetPlayers(true))
        {
            if (!_host.IsAlive(p)) continue;

            double centerX = p.getLocation().getX();
            double centerZ = p.getLocation().getZ();
            double ringY   = _voidLevel + 0.5;

            for (int i = 0; i < 12; i++)
            {
                double angle = (2 * Math.PI / 12) * i + (_particleTick * 0.05);
                double rx    = centerX + Math.cos(angle) * 4.0;
                double rz    = centerZ + Math.sin(angle) * 4.0;

                p.getWorld().spawnParticle(Particle.DUST,
                    new org.bukkit.Location(p.getWorld(), rx, ringY, rz),
                    1, 0, 0, 0, 0, dust);
            }

            // Upward void tendrils if player is close to or below void
            if (p.getLocation().getY() - _voidLevel < 5)
            {
                p.getWorld().spawnParticle(Particle.SOUL,
                    new org.bukkit.Location(p.getWorld(), centerX + (Math.random()-0.5)*4, ringY, centerZ + (Math.random()-0.5)*4),
                    1, 0.1, 0.5, 0.1, 0.02);
            }
        }
    }

    private void updateBossBar()
    {
        if (_voidBossBar == null) return;

        int range    = _mapTopY - _host.WorldData.MinY;
        int climbed  = _voidLevel - _host.WorldData.MinY;
        float progress = range <= 0 ? 0f : Math.max(0f, Math.min(1f, (float) climbed / range));

        _voidBossBar.progress(progress);
        _voidBossBar.name(
            Component.text("🌑 Void Level: ").color(NamedTextColor.DARK_PURPLE).decorate(TextDecoration.BOLD)
                .append(Component.text("Y=" + _voidLevel).color(NamedTextColor.WHITE))
                .append(Component.text("  ▲ Stay High").color(NamedTextColor.GRAY))
        );

        // BossBar color shifts as void rises
        if (progress > 0.7f)
            _voidBossBar.color(BossBar.Color.RED);
        else if (progress > 0.4f)
            _voidBossBar.color(BossBar.Color.PINK);
        else
            _voidBossBar.color(BossBar.Color.PURPLE);

        // Show to new players
        for (Player p : UtilServer.getPlayers())
            p.showBossBar(_voidBossBar);
    }

    public void cleanUp()
    {
        _active = false;
        if (_voidBossBar != null)
        {
            for (Player p : UtilServer.getPlayers())
                p.hideBossBar(_voidBossBar);
            _voidBossBar = null;
        }
    }
}
