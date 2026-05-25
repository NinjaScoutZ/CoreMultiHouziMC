package com.houzicore.lobby.hub.modules;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.core.lang.LangManager;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

/**
 * HubBossBarManager — Rotating tips/info BossBar for the Lobby.
 *
 * Cycles through bilingual tips every ~8 seconds.
 * Each tip has its own bar color for visual variety.
 */
public class HubBossBarManager extends MiniPlugin {

    private final HashMap<UUID, BossBar> _bars = new HashMap<>();
    private int _tick = 0;
    private int _currentTip = 0;

    // ── Tip definitions ─────────────────────────────────────────────────

    private static final String[][] TIPS_TH = {
        {"§f🎮 §eเลือกเกมด้วยเข็มทิศในช่องแรก!", "BLUE"},
        {"§f💎 §bEssence §7สามารถใช้ซื้อเครื่องสำอางได้", "PURPLE"},
        {"§f🏆 §aลองแข่ง Parkour Challenge ใน Lobby!", "GREEN"},
        {"§f🎵 §dเปิด/ปิดวิทยุด้วยคำสั่ง §f/radio", "PINK"},
        {"§f👤 §eดูโปรไฟล์ด้วยหัวผู้เล่นในช่อง 2", "YELLOW"},
    };

    private static final String[][] TIPS_EN = {
        {"§f🎮 §eSelect a game with the compass!", "BLUE"},
        {"§f💎 §bEssence §7can be used to buy cosmetics", "PURPLE"},
        {"§f🏆 §aTry the Parkour Challenge in Lobby!", "GREEN"},
        {"§f🎵 §dToggle the radio with §f/radio", "PINK"},
        {"§f👤 §eView your profile with the head in slot 2", "YELLOW"},
    };

    public HubBossBarManager(JavaPlugin plugin) {
        super("Hub BossBar", plugin);
    }

    // ── Player lifecycle ────────────────────────────────────────────────

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        BossBar bar = createBar(player);
        _bars.put(player.getUniqueId(), bar);
        bar.addPlayer(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        BossBar bar = _bars.remove(event.getPlayer().getUniqueId());
        if (bar != null) {
            bar.removeAll();
        }
    }

    // ── Tick cycle (every 500ms → 16 ticks = ~8 seconds) ────────────────

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (event.getType() != UpdateType.FASTER) return;

        _tick++;
        if (_tick < 16) return; // 16 × 500ms = 8 seconds
        _tick = 0;

        _currentTip = (_currentTip + 1) % TIPS_EN.length;

        for (Player player : UtilServer.getPlayers()) {
            BossBar bar = _bars.get(player.getUniqueId());
            if (bar == null) {
                bar = createBar(player);
                _bars.put(player.getUniqueId(), bar);
                bar.addPlayer(player);
                continue;
            }

            boolean isThai = LangManager.get().isThai(player);
            String[][] tips = isThai ? TIPS_TH : TIPS_EN;

            // Cancel old bar and set new content (per project rules: cancel before set)
            bar.setTitle(tips[_currentTip][0]);
            bar.setColor(parseColor(tips[_currentTip][1]));
            bar.setProgress(1.0);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private BossBar createBar(Player player) {
        boolean isThai = LangManager.get().isThai(player);
        String[][] tips = isThai ? TIPS_TH : TIPS_EN;

        BossBar bar = Bukkit.createBossBar(
            tips[_currentTip][0],
            parseColor(tips[_currentTip][1]),
            BarStyle.SOLID
        );
        bar.setProgress(1.0);
        return bar;
    }

    private BarColor parseColor(String name) {
        switch (name) {
            case "BLUE":   return BarColor.BLUE;
            case "PURPLE": return BarColor.PURPLE;
            case "GREEN":  return BarColor.GREEN;
            case "PINK":   return BarColor.PINK;
            case "YELLOW": return BarColor.YELLOW;
            case "RED":    return BarColor.RED;
            default:       return BarColor.BLUE;
        }
    }

    @Override
    public void disable() {
        for (BossBar bar : _bars.values()) {
            bar.removeAll();
        }
        _bars.clear();
    }
}
