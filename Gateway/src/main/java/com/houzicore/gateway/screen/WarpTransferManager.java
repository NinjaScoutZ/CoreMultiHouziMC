package com.houzicore.gateway.screen;

import com.houzicore.gateway.GatewayPlugin;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Warp sequence after successful authentication.
 *
 * <h3>Design principle: No Fake</h3>
 * <p>Every title/subtitle shown to the player must correspond to something that is
 * ACTUALLY happening in this JVM at that tick. We never claim a remote server is
 * "loading" or that we are "authenticating" when auth is already done.
 *
 * <h3>Flight ownership</h3>
 * <p>LoginScreenManager.remove() deliberately does NOT drop flight. This class
 * is the sole owner of flight state from auth-complete through BungeeCord transfer.
 *
 * <h3>Sequence</h3>
 * <pre>
 * Phase A — Auth confirmation (tick 0-8)
 *   tick  0  Blindness removed. Title: "✓ เข้าสู่ระบบสำเร็จ"
 *              Subtitle: "กำลังเตรียมการโอนย้าย..."   ← we are actually preparing state
 *
 * Phase B — Suck-in cinematic (tick 8-22, no fake text)
 *   tick  8  Sound: ENDERMAN_TELEPORT. Spiral phase 1 (r=2.5, PORTAL)
 *   tick 13  Sound: ENDER_EYE_LAUNCH.  Spiral phase 2 (r=1.5, END_ROD + ENCHANTED_HIT)
 *   tick 18  Sound: ENDER_EYE_DEATH.   Implosion at center (CRIT + FLASH)
 *   tick 22  Sound: PORTAL_TRIGGER (short). No title change — cinematic only.
 *
 * Phase C — Actual send (tick 24)
 *   tick 24  Title: "⟶ กำลังส่งคำขอ" / "ส่ง plugin message ไปยัง BungeeCord..."
 *            sendToServer() called HERE (fire-and-forget)
 *            Flight dropped, SLOW_FALLING removed — player about to leave server
 *            → Waiting runnable starts (see below)
 *
 * Phase D — Honest wait loop (separate runnable, starts at tick 24)
 *   Every 10 ticks: action bar updates "⟳ รอ BungeeCord... (Xs)"
 *   At 3s elapsed: "⚠ ใช้เวลานาน — กำลังลองใหม่..."  + retry sendToServer()
 *   At 6s elapsed: kick with explanation message
 * </pre>
 */
public class WarpTransferManager {

    /** After this many seconds of no transfer, retry. */
    private static final int RETRY_SECONDS  = 3;
    /** After this many seconds of no transfer, kick the player. */
    private static final int TIMEOUT_SECONDS = 6;

    private final GatewayPlugin plugin;

    public WarpTransferManager(GatewayPlugin plugin) {
        this.plugin = plugin;
    }

    // -----------------------------------------------------------------------
    // Public entry point
    // -----------------------------------------------------------------------

    /**
     * Call this after a player has been fully authenticated.
     * Pre-condition: player is flying (LoginScreenManager set flight, remove() left it).
     */
    public void warpToLobby(Player player) {
        final String displayName = plugin.getGateConfig().lobbyDisplayName(); // e.g. "§bLobby"
        final String lobbyServer = plugin.getGateConfig().lobbyServer();

        // Restore fly speed to slow drift — feels weightless before the pull.
        // (LoginScreenManager zeroed fly-speed to lock movement during auth.)
        player.setFlySpeed(0.05f);

        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                if (!player.isOnline()) { cancel(); return; }

                switch (tick) {

                    // ── Phase A: Auth confirmation ──────────────────────────
                    case 0 -> {
                        // Auth is DONE. That's the truth. Say so.
                        player.removePotionEffect(
                                org.bukkit.potion.PotionEffectType.BLINDNESS);
                        player.sendTitle(
                                "§a§l✓ เข้าสู่ระบบสำเร็จ",
                                "§7กำลังเตรียมการโอนย้าย...",
                                5, 60, 5);
                        // Subtle confirmation chime
                        player.playSound(player.getLocation(),
                                Sound.UI_TOAST_IN, SoundCategory.MASTER, 1.0f, 1.4f);
                    }

                    // ── Phase B: Cinematic — no status claim ────────────────
                    // Titles stay from Phase A. We don't claim anything is
                    // "connecting" or "loading" because nothing is yet.

                    case 8 -> {
                        // Phase B begins — suck-in outer ring
                        player.playSound(player.getLocation(),
                                Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.MASTER, 0.9f, 0.6f);
                        // SOUL_FIRE_FLAME: confirmed no-data in Paper 1.21.11
                        // (Particle.PORTAL replaced — it requires data in some 1.21 builds)
                        spawnInwardSpiral(player,
                                player.getLocation().add(0, 1.0, 0),
                                2.5, 24, Particle.SOUL_FIRE_FLAME, 0.0f);
                        // Subtle sound to start building tension
                        player.playSound(player.getLocation(),
                                Sound.BLOCK_PORTAL_TRIGGER, SoundCategory.MASTER, 0.3f, 1.8f);
                    }

                    case 13 -> {
                        // Phase B mid — tighter rings
                        player.playSound(player.getLocation(),
                                Sound.ENTITY_ENDER_EYE_LAUNCH, SoundCategory.MASTER, 0.7f, 1.4f);
                        spawnInwardSpiral(player,
                                player.getLocation().add(0, 1.0, 0),
                                1.5, 32, Particle.END_ROD, 0.0f);
                        spawnInwardSpiral(player,
                                player.getLocation().add(0, 1.0, 0),
                                1.5, 20, Particle.ENCHANTED_HIT, 0.0f);
                    }

                    case 18 -> {
                        // Phase B implosion — particles collapse to center
                        player.playSound(player.getLocation(),
                                Sound.ENTITY_ENDER_EYE_DEATH, SoundCategory.MASTER, 1.2f, 1.3f);
                        Location center = player.getLocation().add(0, 1.0, 0);
                        spawnInwardSpiral(player, center, 0.4, 40, Particle.END_ROD, 0.0f);
                        player.spawnParticle(Particle.CRIT,
                                center, 60, 0.25, 0.4, 0.25, 0.4);
                        player.spawnParticle(Particle.ENCHANTED_HIT,
                                center, 40, 0.2, 0.3, 0.2, 0.3);
                        // FLASH removed: requires org.bukkit.Color data in Paper 1.21.11
                        // END_ROD burst at center gives similar white-flash feel without data
                        player.spawnParticle(Particle.END_ROD,
                                center, 20, 0.05, 0.05, 0.05, 0.15);
                    }

                    // ── Phase C: ACTUAL send (tick 24) ──────────────────────
                    case 24 -> {
                        // Update title BEFORE sending — so the text matches the action.
                        player.sendTitle(
                                "§b§l⟶ กำลังส่งคำขอ",
                                "§7ส่ง plugin message ไปยัง BungeeCord...",
                                5, 120, 5);

                        // Drop flight here — we're done with the auth screen state.
                        player.removePotionEffect(
                                org.bukkit.potion.PotionEffectType.SLOW_FALLING);
                        player.setFlySpeed(0.1f);
                        player.setFlying(false);
                        player.setAllowFlight(false);

                        // THIS is the real action. Happens at the same tick as the title.
                        doSendToServer(player, lobbyServer);

                        // Start honest waiting loop — we don't know when BungeeCord
                        // will process this. Track elapsed time and show it truthfully.
                        startWaitingLoop(player, displayName, lobbyServer);

                        cancel(); // cinematic runnable's job is done
                    }
                }
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    // -----------------------------------------------------------------------
    // Honest waiting loop
    // Runs AFTER the BungeeCord message has been sent.
    // Shows elapsed wait time. Retries at RETRY_SECONDS. Kicks at TIMEOUT_SECONDS.
    // If BungeeCord works correctly the player will be gone long before any of this.
    // -----------------------------------------------------------------------

    private void startWaitingLoop(Player player, String displayName, String lobbyServer) {
        new BukkitRunnable() {
            /** Elapsed ticks since sendToServer was called. */
            int waited = 0;
            boolean retried = false;

            @Override
            public void run() {
                if (!player.isOnline()) { cancel(); return; }

                waited += 10; // runs every 10 ticks = 0.5s
                double seconds = waited / 20.0;

                if (!retried && seconds >= RETRY_SECONDS) {
                    // Player still on Gateway after RETRY_SECONDS — unusual.
                    // Honest message: we're trying again, not silent failure.
                    retried = true;
                    player.sendTitle(
                            "§e§l⚠ กำลังพยายาม...",
                            "§7BungeeCord ยังไม่ตอบสนอง — ลองใหม่อีกครั้ง",
                            5, 60, 5);
                    player.playSound(player.getLocation(),
                            Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 0.6f, 0.6f);
                    doSendToServer(player, lobbyServer); // retry
                }

                if (seconds >= TIMEOUT_SECONDS) {
                    // Still here after TIMEOUT_SECONDS — give up honestly.
                    // Tell the player exactly what happened and what to do.
                    player.sendTitle(" ", " ", 0, 0, 0);
                    player.sendMessage(plugin.getGateConfig().prefix()
                            + "§c⚠ ไม่สามารถโอนย้ายได้อัตโนมัติ");
                    player.sendMessage(plugin.getGateConfig().prefix()
                            + "§7BungeeCord ไม่ตอบสนองภายใน " + TIMEOUT_SECONDS + " วินาที");
                    player.sendMessage(plugin.getGateConfig().prefix()
                            + "§eคุณได้รับการยืนยันตัวตนแล้ว — กรุณาเชื่อมต่อไปยัง "
                            + displayName + " §eด้วยตัวเอง");
                    player.setFlySpeed(0.1f);
                    player.setFlying(false);
                    player.setAllowFlight(false);
                    cancel();
                    return;
                }

                // Normal wait: show elapsed time honestly in action bar.
                // "รอ BungeeCord... (1.5s)" — player knows exactly how long we've been waiting.
                String waitMsg = retried
                        ? "§c⟳ รอการตอบสนอง (พยายามซ้ำ)... §7(" + format(seconds) + "s)"
                        : "§e⟳ รอ BungeeCord... §7(" + format(seconds) + "s)";

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent(waitMsg));
            }
        }.runTaskTimer(plugin, 10L, 10L); // start 0.5s after send, tick every 0.5s
    }

    // -----------------------------------------------------------------------
    // BungeeCord transfer (fire-and-forget — we cannot know if it succeeds)
    // -----------------------------------------------------------------------

    @SuppressWarnings("UnstableApiUsage")
    private void doSendToServer(Player player, String server) {
        // 2-tick delay: plugin messages sent in the same tick as state changes
        // (e.g., inventory close) can be silently dropped by BungeeCord.
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;
                try {
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("Connect");
                    out.writeUTF(server);
                    player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
                } catch (Exception e) {
                    plugin.getLogger().warning(
                            "doSendToServer failed for " + player.getName() + ": " + e.getMessage());
                    // Waiting loop will handle retry / timeout — don't double-message here.
                }
            }
        }.runTaskLater(plugin, 2L);
    }

    // -----------------------------------------------------------------------
    // Suck-in spiral
    // Spawns a ring of particles at radius r, each given an inward velocity
    // so the client renders them moving toward the center ("vacuum pull" feel).
    // Player-only: other players in the same world are not affected.
    // -----------------------------------------------------------------------

    private void spawnInwardSpiral(Player player, Location center,
                                   double r, int count, Particle type, float speed) {
        for (int i = 0; i < count; i++) {
            double angle = Math.toRadians(i * (360.0 / count));
            double px    = center.getX() + r * Math.cos(angle);
            double py    = center.getY() + (i % 4) * 0.12;
            double pz    = center.getZ() + r * Math.sin(angle);

            // Inward direction vector
            double dx  = center.getX() - px;
            double dz  = center.getZ() - pz;
            double len = Math.sqrt(dx * dx + dz * dz);
            if (len > 0) { dx = (dx / len) * 0.35; dz = (dz / len) * 0.35; }

            player.spawnParticle(type,
                    new Location(center.getWorld(), px, py, pz),
                    1, dx, 0, dz, speed);
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static String format(double seconds) {
        // Show 1 decimal place, e.g. "1.5"
        return String.format("%.1f", seconds);
    }
}
