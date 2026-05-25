package com.houzicore.gateway.screen;

import com.houzicore.gateway.GatewayPlugin;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * Manages the blank/dark login screen experience.
 *
 * On join (before auth):
 *  - Set player to Adventure mode (prevents block interaction/breaking)
 *  - Teleport player to maximum world height (sky ceiling)
 *  - Apply permanent Blindness so they see only black
 *  - Apply Slow Falling to prevent dropping
 *  - Send action bar login prompt every 2s
 *
 * Called from LoginListener after session is opened.
 */
public class LoginScreenManager {

    private final GatewayPlugin plugin;

    public LoginScreenManager(GatewayPlugin plugin) {
        this.plugin = plugin;
    }

    // -----------------------------------------------------------------------
    // Apply login screen to a freshly joined player
    // -----------------------------------------------------------------------

    public void apply(Player player) {
        // Run on next tick to ensure effects and teleport apply properly after join
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;
                
                // 1. Set adventure mode — no breaking/placing blocks
                player.setGameMode(GameMode.ADVENTURE);

                // 2. Teleport to max sky height — nothing visible
                World world = player.getWorld();
                int maxY = world.getMaxHeight() - 1; // typically 319 for 1.21
                Location skySpawn = new Location(world, 0.5, maxY, 0.5);
                skySpawn.setYaw(0);
                skySpawn.setPitch(90); // look straight down (adds to disorientation)
                player.teleport(skySpawn);

                // 3. Lock the player in the sky — no falling, no moving
                player.setAllowFlight(true);
                player.setFlying(true);
                player.setFlySpeed(0f); // zero flight speed so they can't move
                player.setWalkSpeed(0f); // prevent walking/FOV glitches

                // 4. Blind the player so the void is truly black
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.BLINDNESS,
                        Integer.MAX_VALUE, 0, false, false, false));

                // 5. Slow falling as safety net if flight somehow drops, plus Levitation 255 to freeze vertical movement
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.SLOW_FALLING,
                        Integer.MAX_VALUE, 0, false, false, false));
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.LEVITATION,
                        Integer.MAX_VALUE, 255, false, false, false));

                // 6. No damage / no hunger
                player.setInvulnerable(true);
                player.setFoodLevel(20);
                player.setSaturation(20f);

                // 7. Hide HUD elements: clear title
                player.sendTitle(" ", " ", 0, 40, 10);

                // 8. Show action-bar prompt repeatedly until authenticated
                startActionBarLoop(player);
            }
        }.runTaskLater(plugin, 1L);
    }

    // -----------------------------------------------------------------------
    // Remove login screen (call after auth completes)
    // -----------------------------------------------------------------------

    public void remove(Player player) {
        // Clear auth-blocking effects only.
        // Flight state is intentionally left intact here — WarpTransferManager
        // owns flight during the warp sequence and will release it just before
        // the BungeeCord transfer fires. Removing flight here would cause the
        // player to fall during the gap between auth and the first warp tick.
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.SLOW_FALLING);
        player.removePotionEffect(PotionEffectType.LEVITATION);
        player.setInvulnerable(false);
        player.setGameMode(GameMode.ADVENTURE);
        player.setWalkSpeed(0.2f); // restore default walk speed
        // Title/action bar get cleared by warp effect taking over.
    }

    // -----------------------------------------------------------------------
    // Action bar loop
    // -----------------------------------------------------------------------

    private void startActionBarLoop(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                if (plugin.getSessionManager().isAuthenticated(player)) {
                    cancel();
                    return;
                }

                String msg = plugin.getGateConfig().actionBarPrompt(player);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent(msg));
            }
        }.runTaskTimer(plugin, 0L, 40L); // every 2 seconds
    }
}
