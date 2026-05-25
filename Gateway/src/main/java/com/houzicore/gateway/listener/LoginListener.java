package com.houzicore.gateway.listener;

import com.houzicore.gateway.GatewayPlugin;
import com.houzicore.gateway.auth.AuthSession;
import com.houzicore.gateway.auth.AuthSession.State;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

/**
 * Handles player join / quit lifecycle for HouziGate.
 */
public class LoginListener implements Listener {

    private final GatewayPlugin plugin;

    public LoginListener(GatewayPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Player player = event.getPlayer();

        // 1. Full admin/dev bypass check
        if (player.hasPermission("houzigate.bypass")) {
            return;
        }

        String name = player.getName();
        String ip   = player.getAddress() != null
                ? player.getAddress().getAddress().getHostAddress() : "unknown";

        // 2. IP ban check (Brute force protection)
        if (plugin.getRateLimiter().isBanned(ip)) {
            player.kickPlayer(plugin.getGateConfig().ipBanned());
            return;
        }

        // Apply login screen immediately (Adventure mode, sky height, blindness, freeze)
        plugin.getLoginScreen().apply(player);

        // Open provisional session
        AuthSession session = plugin.getSessionManager().open(player, State.WAITING_LOGIN);

        // 3. Premium Bypass check (UUID verification against offline UUID)
        if (isPremiumPlayer(player)) {
            handlePremiumJoin(player, name, ip, session);
        } else {
            // Cracked player path
            handleCrackedJoin(player, name, ip, session);
        }
    }

    private boolean isPremiumPlayer(Player player) {
        if (!plugin.getGateConfig().premiumBypass()) {
            return false;
        }
        // If a player joined through proxy's online mode connection, their UUID
        // will be the genuine Mojang UUID. If offline mode/spoofed, it will be offline UUID.
        UUID offlineUuid = UUID.nameUUIDFromBytes(
                ("OfflinePlayer:" + player.getName()).getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
        return !player.getUniqueId().equals(offlineUuid);
    }

    private void handlePremiumJoin(Player player, String name, String ip, AuthSession session) {
        boolean registered = plugin.getDatabase().accountExists(name);

        if (!registered) {
            // First time premium player — auto register them securely
            String randomPass = UUID.randomUUID().toString();
            String hash = BCrypt.hashpw(randomPass, BCrypt.gensalt(10));
            plugin.getDatabase().createAccount(name, hash, true);
            plugin.getLogger().info("[Auth] Auto-registered new Premium player: " + name);
        } else {
            // Existing account — verify premium flag in DB
            if (!plugin.getDatabase().isPremium(name)) {
                plugin.getDatabase().setPremiumFlag(name, true);
            }
        }

        session.setPremiumDetected(true);

        // 2FA check
        boolean ipTrusted  = plugin.getIpTrustManager().isTrusted(name, ip);
        boolean needsTwoFa = plugin.getGateConfig().twoFaEnabled()
                && isStaff(player) && !ipTrusted;

        if (needsTwoFa) {
            session.setState(State.WAITING_2FA);
            player.sendMessage(plugin.getGateConfig().prefix() + plugin.getGateConfig().newIpNotice());
            plugin.getDialogLoginUI().openFor(player);
        } else {
            // Fully bypass & login
            plugin.getDatabase().logLogin(name, ip, "PREMIUM_BYPASS");
            
            plugin.getSessionManager().markAuthenticated(player);
            plugin.getDatabase().updateLastLogin(name, ip);
            if (!plugin.getIpTrustManager().isTrusted(name, ip)) {
                plugin.getIpTrustManager().trust(name, ip);
            }

            // Create IP session
            plugin.getSessionLoginManager().createSession(name, ip);

            player.sendMessage(plugin.getGateConfig().prefix() + plugin.getGateConfig().premiumDetected());
            plugin.getLoginScreen().remove(player);
            plugin.getWarpTransfer().warpToLobby(player);
        }
    }

    private void handleCrackedJoin(Player player, String name, String ip, AuthSession session) {
        // Session Login recall check (AuthMe style)
        if (plugin.getSessionLoginManager().isValidSession(name, ip)) {
            plugin.getDatabase().logLogin(name, ip, "SESSION_BYPASS");
            
            plugin.getSessionManager().markAuthenticated(player);
            plugin.getDatabase().updateLastLogin(name, ip);

            player.sendMessage(plugin.getGateConfig().prefix() + plugin.getGateConfig().loginSuccess() + " (Auto-Login)");
            plugin.getLoginScreen().remove(player);
            plugin.getWarpTransfer().warpToLobby(player);
            return;
        }

        // Fallback to normal DialogUI prompt
        boolean registered = plugin.getDatabase().accountExists(name);
        session.setState(registered ? State.WAITING_LOGIN : State.WAITING_REGISTER);

        plugin.getDialogLoginUI().openFor(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        Player player = event.getPlayer();
        plugin.getSessionManager().close(player.getUniqueId());
        plugin.getDialogLoginUI().cleanup(player.getUniqueId());
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (!plugin.getSessionManager().isAuthenticated(player)) {
            event.setCancelled(true);
        }
    }

    private boolean isStaff(Player player) {
        for (String perm : plugin.getGateConfig().staffPermissions()) {
            if (player.hasPermission(perm)) return true;
        }
        return false;
    }
}
