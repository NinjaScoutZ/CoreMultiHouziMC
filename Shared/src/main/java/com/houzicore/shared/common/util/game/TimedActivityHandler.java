package com.houzicore.shared.common.util.game;

import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Consumer;

/**
 * A Builder-based state machine for managing timed game activities.
 * Handles countdown → activity → completion/cancellation with clean callbacks.
 *
 * <pre>
 * TimedActivityHandler.builder()
 *     .player(player)
 *     .countdown(3)
 *     .duration(30)
 *     .onCountdownTick(remaining -> player.sendTitle("§e" + remaining, "", 0, 25, 0))
 *     .onStart(() -> player.sendMessage("เริ่ม!"))
 *     .onTick(elapsed -> updateProgressBar(player, elapsed, 30))
 *     .onComplete(() -> giveReward(player))
 *     .onCancel(() -> player.sendMessage("§cยกเลิก!"))
 *     .cancelOnMove(true)
 *     .build()
 *     .start(plugin);
 * </pre>
 *
 * State Machine: IDLE → COUNTING → IN_PROGRESS → COMPLETED | CANCELLED
 *
 * Ported from: net.swofty.type.generic.utility.TimedActivityHandler
 * Adapted to use Bukkit UpdateEvent instead of Minestom scheduler.
 */
public class TimedActivityHandler implements Listener {

    public enum ActivityState {
        IDLE, COUNTING, IN_PROGRESS, COMPLETED, CANCELLED
    }

    // --- Config (set by builder) ---
    private final Player player;
    private final int countdownSeconds;
    private final int durationSeconds;
    private final Consumer<Integer> onCountdownTick;  // receives remaining seconds
    private final Runnable onStart;
    private final Consumer<Integer> onTick;            // receives elapsed seconds
    private final Runnable onComplete;
    private final Runnable onCancel;
    private final boolean cancelOnMove;
    private final boolean cancelOnDamage;

    // --- Runtime state ---
    private ActivityState state = ActivityState.IDLE;
    private int tickCounter = 0;    // counts seconds elapsed in current phase
    private Location startLocation; // for cancelOnMove check

    private TimedActivityHandler(Builder builder) {
        this.player = builder.player;
        this.countdownSeconds = builder.countdownSeconds;
        this.durationSeconds = builder.durationSeconds;
        this.onCountdownTick = builder.onCountdownTick;
        this.onStart = builder.onStart;
        this.onTick = builder.onTick;
        this.onComplete = builder.onComplete;
        this.onCancel = builder.onCancel;
        this.cancelOnMove = builder.cancelOnMove;
        this.cancelOnDamage = builder.cancelOnDamage;
    }

    public static Builder builder() {
        return new Builder();
    }

    public ActivityState getState() {
        return state;
    }

    /**
     * Start the activity. Registers listeners and begins countdown (or activity if no countdown).
     */
    public void start(JavaPlugin plugin) {
        if (state != ActivityState.IDLE) return; // prevent double-start

        if (player != null) {
            startLocation = player.getLocation().clone();
        }
        Bukkit.getPluginManager().registerEvents(this, plugin);

        if (countdownSeconds > 0) {
            state = ActivityState.COUNTING;
            tickCounter = 0;
        } else {
            beginActivity();
        }
    }

    /**
     * Cancel the activity from external code.
     */
    public void cancel() {
        if (state == ActivityState.COMPLETED || state == ActivityState.CANCELLED) return;
        state = ActivityState.CANCELLED;
        if (onCancel != null) onCancel.run();
        cleanup();
    }

    // --- Event Handlers ---

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (event.getType() != UpdateType.SEC) return;
        if (player != null && !player.isOnline()) { cancel(); return; }

        switch (state) {
            case COUNTING -> {
                tickCounter++;
                int remaining = countdownSeconds - tickCounter;
                if (onCountdownTick != null) onCountdownTick.accept(remaining);
                if (remaining <= 0) {
                    beginActivity();
                }
            }
            case IN_PROGRESS -> {
                tickCounter++;
                if (onTick != null) onTick.accept(tickCounter);
                if (tickCounter >= durationSeconds) {
                    state = ActivityState.COMPLETED;
                    if (onComplete != null) onComplete.run();
                    cleanup();
                }
            }
            default -> {} // IDLE, COMPLETED, CANCELLED — do nothing
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!cancelOnMove || player == null) return;
        if (state != ActivityState.COUNTING && state != ActivityState.IN_PROGRESS) return;
        if (!event.getPlayer().equals(player)) return;

        // Only cancel on actual block movement, not head rotation
        if (event.getFrom().getBlockX() != event.getTo().getBlockX()
            || event.getFrom().getBlockY() != event.getTo().getBlockY()
            || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            cancel();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!cancelOnDamage || player == null) return;
        if (state != ActivityState.COUNTING && state != ActivityState.IN_PROGRESS) return;
        if (!event.getEntity().equals(player)) return;

        cancel();
    }

    // --- Internal ---

    private void beginActivity() {
        state = ActivityState.IN_PROGRESS;
        tickCounter = 0;
        if (onStart != null) onStart.run();
    }

    private void cleanup() {
        HandlerList.unregisterAll(this);
    }

    // ═══════════════════════════════════════
    // Builder
    // ═══════════════════════════════════════

    public static class Builder {
        private Player player;
        private int countdownSeconds = 0;
        private int durationSeconds = 10;
        private Consumer<Integer> onCountdownTick;
        private Runnable onStart;
        private Consumer<Integer> onTick;
        private Runnable onComplete;
        private Runnable onCancel;
        private boolean cancelOnMove = false;
        private boolean cancelOnDamage = false;

        public Builder player(Player player) { this.player = player; return this; }
        public Builder countdown(int seconds) { this.countdownSeconds = seconds; return this; }
        public Builder duration(int seconds) { this.durationSeconds = seconds; return this; }
        public Builder onCountdownTick(Consumer<Integer> cb) { this.onCountdownTick = cb; return this; }
        public Builder onStart(Runnable cb) { this.onStart = cb; return this; }
        public Builder onTick(Consumer<Integer> cb) { this.onTick = cb; return this; }
        public Builder onComplete(Runnable cb) { this.onComplete = cb; return this; }
        public Builder onCancel(Runnable cb) { this.onCancel = cb; return this; }
        public Builder cancelOnMove(boolean val) { this.cancelOnMove = val; return this; }
        public Builder cancelOnDamage(boolean val) { this.cancelOnDamage = val; return this; }

        public TimedActivityHandler build() {
            return new TimedActivityHandler(this);
        }
    }
}
