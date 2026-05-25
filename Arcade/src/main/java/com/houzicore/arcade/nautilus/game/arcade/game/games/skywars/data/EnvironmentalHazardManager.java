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
import org.bukkit.scheduler.BukkitRunnable;

import com.houzicore.shared.common.util.game.TimedActivityHandler;
import java.util.ArrayList;
import java.util.List;

/**
 * EnvironmentalHazardManager — master controller for all random hazard events in Skywars.
 *
 * Timeline:
 *   T+90s  → Minor Hazard #1 (Meteor Shower)
 *   T+180s → Minor Hazard #2 (random)
 *   T+230s → Endgame Roulette Animation starts (T-10s)
 *   T+240s → Endgame Event starts (Crumble OR Void Rising, decided by roulette)
 *   T+270s → Dragon Strafing Run (always)
 */
public class EnvironmentalHazardManager
{
    private final Skywars _host;
    private final JavaPlugin _plugin;

    private final List<TimedActivityHandler> _handlers = new ArrayList<>();

    // Active endgame event
    private VoidRisingEvent _voidRising;

    // Was Void Rising selected? (false = Crumble)
    private boolean _voidRisingSelected = false;

    // Dragon
    private DragonStrafingEvent _dragonStrafing;

    // Active meteor shower
    private MeteorShowerEvent _meteorShower;

    // BossBar for roulette
    private BossBar _rouletteBossBar;

    // Timings (ms after game goes Live)
    private static final long T_MINOR_1  = 240_000L; // 4m
    private static final long T_MINOR_2  = 480_000L; // 8m
    private static final long T_ROULETTE = 710_000L; // 11m 50s
    private static final long T_ENDGAME  = 720_000L; // 12m
    private static final long T_DRAGON   = 780_000L; // 13m

    public EnvironmentalHazardManager(Skywars host, JavaPlugin plugin)
    {
        _host   = host;
        _plugin = plugin;
        startTimers();
    }

    private void startTimers()
    {
        // 4m: Minor Hazard 1
        _handlers.add(TimedActivityHandler.builder().countdown(240).onStart(this::triggerMeteorShower).build());
        
        // 8m: Minor Hazard 2
        _handlers.add(TimedActivityHandler.builder().countdown(480).onStart(this::triggerMeteorShower).build());
        
        // 11m 50s: Roulette
        _handlers.add(TimedActivityHandler.builder().countdown(710).onStart(() -> {
            _voidRisingSelected = Math.random() < 0.5; // 50/50
            startRouletteAnimation(_voidRisingSelected);
        }).build());
        
        // 12m: Endgame Lock-in
        _handlers.add(TimedActivityHandler.builder().countdown(720).onStart(() -> {
            if (_voidRisingSelected) {
                _voidRising = new VoidRisingEvent(_host, _plugin);
                _voidRising.start();
            }
        }).build());
        
        // 13m: Dragon
        _handlers.add(TimedActivityHandler.builder().countdown(780).onStart(() -> {
            _dragonStrafing = new DragonStrafingEvent(_host, _plugin);
            _dragonStrafing.start();
        }).build());

        for (TimedActivityHandler handler : _handlers) {
            handler.start(_plugin);
        }
    }

    /** Called every TICK from Skywars. (Now only ticks active custom events, not timeline) */
    public void tick()
    {
        // Tick active events
        if (_meteorShower != null && _meteorShower.isActive())
            _meteorShower.tick();

        if (_voidRising != null && _voidRising.isActive())
            _voidRising.tick();

        if (_dragonStrafing != null && _dragonStrafing.isActive())
            _dragonStrafing.tick();
    }

    // ─── Meteor Shower ────────────────────────────────────────────
    private void triggerMeteorShower()
    {
        // Warning 3s before
        for (Player p : UtilServer.getPlayers())
        {
            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_AMBIENT, 1.5f, 0.5f);
        }

        for (Player p : UtilServer.getPlayers())
        {
            boolean th = p != null && "THA".equals(com.houzicore.shared.core.lang.LangManager.get().resolveLocaleStr(p));
            p.sendMessage(com.houzicore.shared.common.util.F.main("Game", th 
                ? "§c§l☄ ฝนดาวตกกำลังจะเริ่มขึ้น! หาที่หลบภัย!" 
                : "§c§l☄ Meteor shower is starting! Take cover!"));
        }

        // Schedule actual shower 3s later (allow warning to show)
        new BukkitRunnable() {
            @Override public void run() {
                if (!_host.IsLive()) return;
                _meteorShower = new MeteorShowerEvent(_host, _plugin);
                _meteorShower.start();
            }
        }.runTaskLater(_plugin, 60L);
    }

    // ─── Endgame Roulette Animation ───────────────────────────────
    private void startRouletteAnimation(boolean voidWins)
    {
        // Create roulette BossBar
        _rouletteBossBar = BossBar.bossBar(
            Component.text("🎲 ROLLING ENDGAME EVENT...").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD),
            1.0f,
            BossBar.Color.YELLOW,
            BossBar.Overlay.NOTCHED_10
        );
        for (Player p : UtilServer.getPlayers())
            p.showBossBar(_rouletteBossBar);

        // Roulette: 20 flips total, starting fast then slowing
        // Schedule sequence of title flips
        final String[] options = {
            C.cRed + C.Bold + "☠ MAP CRUMBLE",
            C.cDPurple + C.Bold + "🌑 THE VOID"
        };

        new BukkitRunnable()
        {
            int _flip    = 0;
            int _maxFlip = 22; // total frames
            int _current = 0;
            final long[] _delays = buildDelaySchedule(_maxFlip);

            @Override public void run()
            {
                if (_flip >= _maxFlip)
                {
                    // Lock-in final result
                    String winner = voidWins ? options[1] : options[0];
                    String sub    = voidWins ? "The Void rises..." : "The islands crumble!";
                    Sound sound   = voidWins ? Sound.ENTITY_WITHER_SPAWN : Sound.ENTITY_ENDER_DRAGON_GROWL;

                    if (_rouletteBossBar != null) {
                        _rouletteBossBar.color(voidWins ? BossBar.Color.PURPLE : BossBar.Color.RED);
                        _rouletteBossBar.name(Component.text("🏆 EVENT: ").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                            .append(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(winner))
                            .append(Component.text(" (" + sub + ")").color(NamedTextColor.GRAY)));
                    }
                    for (Player p : UtilServer.getPlayers()) {
                        boolean th = p != null && "THA".equals(com.houzicore.shared.core.lang.LangManager.get().resolveLocaleStr(p));
                        p.sendMessage(com.houzicore.shared.common.util.F.main("Game", th
                            ? (voidWins ? "§5§l🌑 การสุ่มเสร็จสิ้น: ความมืดกำลังมาเยือน! (The Void)" : "§c§l☠ การสุ่มเสร็จสิ้น: เกาะกำลังถล่ม! (Map Crumble)")
                            : (voidWins ? "§5§l🌑 Roulette: The Void rises!" : "§c§l☠ Roulette: The islands crumble!")));
                    }

                    for (Player p : UtilServer.getPlayers())
                    {
                        p.playSound(p.getLocation(), sound, 3.0f, 1.0f);

                        // Screen shake via velocity nudge
                        org.bukkit.util.Vector shake = new org.bukkit.util.Vector(
                            (Math.random() - 0.5) * 0.15,
                            0.05,
                            (Math.random() - 0.5) * 0.15);
                        p.setVelocity(p.getVelocity().add(shake));

                        // Lock-in particle burst
                        Particle.DustOptions dust = voidWins
                            ? new Particle.DustOptions(org.bukkit.Color.fromRGB(100, 0, 200), 1.5f)
                            : new Particle.DustOptions(org.bukkit.Color.fromRGB(200, 0, 0), 1.5f);
                        p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 1, 0), 40, 0.5, 1.0, 0.5, 0, dust);
                    }

                    // Remove roulette BossBar after 3s
                    new BukkitRunnable() {
                        @Override public void run() {
                            if (_rouletteBossBar != null)
                                for (Player p : UtilServer.getPlayers())
                                    p.hideBossBar(_rouletteBossBar);
                        }
                    }.runTaskLater(_plugin, 60L);

                    cancel();
                    return;
                }

                // Show alternating title
                String label = options[_current % 2];
                String sub   = _current % 2 == 0 ? "Islands crumbling..." : "Void approaches...";
                if (_rouletteBossBar != null) {
                    _rouletteBossBar.name(Component.text("🎲 ROLL: ").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                        .append(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(label))
                        .append(Component.text(" (" + sub + ")").color(NamedTextColor.GRAY)));
                }

                // Roulette particle per player (red/purple alternating)
                Particle.DustOptions dust = (_current % 2 == 0)
                    ? new Particle.DustOptions(org.bukkit.Color.fromRGB(200, 0, 0), 1.2f)
                    : new Particle.DustOptions(org.bukkit.Color.fromRGB(100, 0, 200), 1.2f);
                for (Player p : UtilServer.getPlayers())
                {
                    p.getWorld().spawnParticle(Particle.DUST,
                        p.getLocation().add(0, 1.5, 0), 8, 0.5, 0.8, 0.5, 0, dust);
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.8f, 1.0f + (_flip * 0.03f));
                }

                _current++;
                _flip++;

                // Reschedule with increasing delay
                long nextDelay = _delays[Math.min(_flip, _delays.length - 1)];
                new BukkitRunnable() {
                    @Override public void run() {
                        EnvironmentalHazardManager.this.runRouletteStep(this, options, voidWins, _flip, _current, _maxFlip, _delays);
                    }
                }.runTaskLater(_plugin, nextDelay);

                cancel(); // Cancel self after spawning next step
            }

            /** Build delay schedule: fast (3 ticks) → slow (20 ticks) over N frames */
            private long[] buildDelaySchedule(int frames)
            {
                long[] d = new long[frames];
                for (int i = 0; i < frames; i++)
                {
                    double t = (double) i / frames;
                    d[i] = (long) (3 + t * t * 17); // 3→20 ticks (eased)
                }
                return d;
            }
        }.runTaskLater(_plugin, 2L);
    }

    /** Recursive roulette step (called by each scheduled task) */
    void runRouletteStep(BukkitRunnable self, String[] options, boolean voidWins,
                         int flip, int current, int maxFlip, long[] delays)
    {
        if (!_host.IsLive()) return;

        if (flip >= maxFlip)
        {
            // Lock-in
            String winner = voidWins ? options[1] : options[0];
            String sub    = voidWins ? "The Void rises..." : "The islands crumble!";
            Sound sound   = voidWins ? Sound.ENTITY_WITHER_SPAWN : Sound.ENTITY_ENDER_DRAGON_GROWL;

            if (_rouletteBossBar != null) {
                _rouletteBossBar.color(voidWins ? BossBar.Color.PURPLE : BossBar.Color.RED);
                _rouletteBossBar.name(Component.text("🏆 EVENT: ").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                    .append(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(winner))
                    .append(Component.text(" (" + sub + ")").color(NamedTextColor.GRAY)));
            }
            for (Player p : UtilServer.getPlayers()) {
                boolean th = p != null && "THA".equals(com.houzicore.shared.core.lang.LangManager.get().resolveLocaleStr(p));
                p.sendMessage(com.houzicore.shared.common.util.F.main("Game", th
                    ? (voidWins ? "§5§l🌑 การสุ่มเสร็จสิ้น: ความมืดกำลังมาเยือน! (The Void)" : "§c§l☠ การสุ่มเสร็จสิ้น: เกาะกำลังถล่ม! (Map Crumble)")
                    : (voidWins ? "§5§l🌑 Roulette: The Void rises!" : "§c§l☠ Roulette: The islands crumble!")));
            }
            for (Player p : UtilServer.getPlayers())
            {
                p.playSound(p.getLocation(), sound, 3.0f, 1.0f);
                org.bukkit.util.Vector shake = new org.bukkit.util.Vector(
                    (Math.random() - 0.5) * 0.15, 0.05, (Math.random() - 0.5) * 0.15);
                p.setVelocity(p.getVelocity().add(shake));
                Particle.DustOptions dust = voidWins
                    ? new Particle.DustOptions(org.bukkit.Color.fromRGB(100, 0, 200), 1.5f)
                    : new Particle.DustOptions(org.bukkit.Color.fromRGB(200, 0, 0), 1.5f);
                p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 1, 0), 40, 0.5, 1.0, 0.5, 0, dust);
            }
            new BukkitRunnable() {
                @Override public void run() {
                    if (_rouletteBossBar != null)
                        for (Player p : UtilServer.getPlayers())
                            p.hideBossBar(_rouletteBossBar);
                }
            }.runTaskLater(_plugin, 60L);
            return;
        }

        // Show flip
        String label = options[current % 2];
        String sub   = current % 2 == 0 ? "Islands crumbling..." : "Void approaches...";
        if (_rouletteBossBar != null) {
            _rouletteBossBar.name(Component.text("🎲 ROLL: ").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD)
                .append(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(label))
                .append(Component.text(" (" + sub + ")").color(NamedTextColor.GRAY)));
        }
        Particle.DustOptions dust = (current % 2 == 0)
            ? new Particle.DustOptions(org.bukkit.Color.fromRGB(200, 0, 0), 1.2f)
            : new Particle.DustOptions(org.bukkit.Color.fromRGB(100, 0, 200), 1.2f);
        for (Player p : UtilServer.getPlayers())
        {
            p.getWorld().spawnParticle(Particle.DUST, p.getLocation().add(0, 1.5, 0), 8, 0.5, 0.8, 0.5, 0, dust);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.8f, 1.0f + (flip * 0.03f));
        }

        int nextFlip    = flip + 1;
        int nextCurrent = current + 1;
        long nextDelay  = delays[Math.min(nextFlip, delays.length - 1)];

        new BukkitRunnable() {
            @Override public void run() {
                runRouletteStep(this, options, voidWins, nextFlip, nextCurrent, maxFlip, delays);
            }
        }.runTaskLater(_plugin, nextDelay);
    }

    /** True if Void Rising was selected (Skywars.java uses this to suppress Crumble). */
    public boolean isVoidRisingActive()
    {
        return _voidRisingSelected;
    }

    public void cleanUp()
    {
        for (TimedActivityHandler handler : _handlers) {
            handler.cancel();
        }
        _handlers.clear();

        if (_rouletteBossBar != null)
            for (Player p : UtilServer.getPlayers())
                p.hideBossBar(_rouletteBossBar);

        if (_meteorShower != null) _meteorShower.cleanUp();
        if (_voidRising   != null) _voidRising.cleanUp();
        if (_dragonStrafing != null) _dragonStrafing.cleanUp();
    }
}
