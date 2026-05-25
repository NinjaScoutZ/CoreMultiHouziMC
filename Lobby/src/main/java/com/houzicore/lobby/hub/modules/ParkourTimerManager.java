package com.houzicore.lobby.hub.modules;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilText;
import com.houzicore.lobby.hub.HubManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import java.time.Duration;

/**
 * Parkour Timer & Checkpoint System (#1 extension)
 * Adds a stopwatch to ParkourManager so players can see their time.
 * Integrates with ParkourManager start/finish NPCs.
 */
public class ParkourTimerManager implements Listener {

    private final HubManager _hub;
    // Maps player UUID to start timestamp (ms)
    private final HashMap<UUID, Long> _startTime = new HashMap<>();
    // Personal best (in-session, not persisted)
    private final HashMap<UUID, Long> _personalBest = new HashMap<>();

    public ParkourTimerManager(HubManager hub) {
        _hub = hub;
        hub.getPlugin().getServer().getPluginManager().registerEvents(this, hub.getPlugin());
    }

    /** Called when player starts parkour */
    public void startTimer(Player player) {
        _startTime.put(player.getUniqueId(), System.currentTimeMillis());
        player.sendMessage(F.main("Parkour", C.cGray + "Timer started! " + C.cYellow + "Run to the finish!"));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
    }

    /** Called when player finishes parkour. Returns elapsed seconds. */
    public double finishTimer(Player player, String courseName) {
        if (!_startTime.containsKey(player.getUniqueId())) return -1;

        long elapsed = System.currentTimeMillis() - _startTime.remove(player.getUniqueId());
        double seconds = elapsed / 1000.0;
        String timeStr = String.format("%.2f", seconds);

        boolean isPB = !_personalBest.containsKey(player.getUniqueId())
                     || elapsed < _personalBest.get(player.getUniqueId());
        if (isPB) _personalBest.put(player.getUniqueId(), elapsed);

        // Title
        String pbText = isPB ? C.cGold + " ★ PB!" : "";
        Title.Times times = Title.Times.times(Duration.ofMillis(300), Duration.ofMillis(3000), Duration.ofMillis(600));
        player.showTitle(Title.title(
            Component.text(UtilText.toSmallCaps("finished!") + pbText, NamedTextColor.GREEN, TextDecoration.BOLD),
            Component.text(courseName + " — " + timeStr + "s", NamedTextColor.GRAY),
            times
        ));

        // Sound
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        _hub.getPlugin().getServer().getScheduler().runTaskLater(_hub.getPlugin(), () ->
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1f, 1.4f), 5L);

        // Broadcast
        _hub.getPlugin().getServer().broadcastMessage(
            F.main("Parkour", C.cYellow + player.getName() + C.cGray + " completed " +
            C.cGreen + courseName + C.cGray + " in " + C.cAqua + timeStr + "s" + pbText));

        return seconds;
    }

    /** Cancels timer if player quits */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        _startTime.remove(event.getPlayer().getUniqueId());
    }

    public boolean hasActiveTimer(Player player) {
        return _startTime.containsKey(player.getUniqueId());
    }
}
