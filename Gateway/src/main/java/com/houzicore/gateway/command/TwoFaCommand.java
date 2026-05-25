package com.houzicore.gateway.command;

import com.houzicore.gateway.GatewayPlugin;
import com.houzicore.gateway.auth.AuthSession;
import com.houzicore.gateway.auth.AuthSession.State;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /2fa <pin>       — verify PIN when session is WAITING_2FA
 * /2fa set <pin>   — set a new 2FA PIN (must be authenticated)
 */
public class TwoFaCommand implements CommandExecutor {

    private final GatewayPlugin plugin;

    public TwoFaCommand(GatewayPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        String prefix = plugin.getGateConfig().prefix();

        if (args.length < 1) {
            player.sendMessage(prefix + "§cUsage: /2fa <pin>  |  /2fa set <pin>");
            return true;
        }

        if (args[0].equalsIgnoreCase("set")) {
            return handleSetPin(player, prefix, args);
        }

        return handleVerifyPin(player, prefix, args[0]);
    }

    // -----------------------------------------------------------------------
    // Set PIN (must be already authenticated)
    // -----------------------------------------------------------------------

    private boolean handleSetPin(Player player, String prefix, String[] args) {
        if (!plugin.getSessionManager().isAuthenticated(player)) {
            player.sendMessage(prefix + "§cYou must be logged in to set a 2FA PIN.");
            return true;
        }
        if (args.length < 2) {
            player.sendMessage(prefix + "§cUsage: /2fa set <pin>");
            return true;
        }

        String rawPin = args[1];
        if (rawPin.length() < plugin.getGateConfig().minPinLength()) {
            player.sendMessage(prefix + plugin.getGateConfig().pinTooShort());
            return true;
        }

        plugin.getTwoFactorManager().setPin(player.getName(), rawPin);
        player.sendMessage(prefix + plugin.getGateConfig().pinSet());
        return true;
    }

    // -----------------------------------------------------------------------
    // Verify PIN → trust IP → remove screen → warp to Lobby
    // -----------------------------------------------------------------------

    private boolean handleVerifyPin(Player player, String prefix, String rawPin) {
        AuthSession session = plugin.getSessionManager().get(player);

        if (session == null || !session.isWaiting2Fa()) {
            player.sendMessage(prefix + "§cNo 2FA challenge is pending.");
            return true;
        }

        // No PIN set yet for this account — auto-trust IP and send through
        if (!plugin.getTwoFactorManager().hasPin(player.getName())) {
            player.sendMessage(prefix + "§eNo PIN set. Use §a/2fa set <pin>§e after connecting.");
            completeAndWarp(player, session);
            return true;
        }

        if (!plugin.getTwoFactorManager().verifyPin(player.getName(), rawPin)) {
            session.incrementFailed();
            int remaining = plugin.getGateConfig().maxAttempts() - session.getFailedAttempts();
            if (remaining <= 0) {
                player.kickPlayer(plugin.getGateConfig().maxAttemptsKick());
                return true;
            }
            player.sendMessage(prefix + plugin.getGateConfig().pinWrong());
            return true;
        }

        // PIN correct
        player.sendMessage(prefix + plugin.getGateConfig().pinCorrect());
        completeAndWarp(player, session);
        return true;
    }

    // -----------------------------------------------------------------------
    // Shared completion: trust IP, authenticate, remove screen, warp
    // -----------------------------------------------------------------------

    private void completeAndWarp(Player player, AuthSession session) {
        String ip = session.getIp();
        plugin.getIpTrustManager().trust(player.getName(), ip);
        plugin.getSessionManager().markAuthenticated(player);
        plugin.getDatabase().updateLastLogin(player.getName(), ip);

        plugin.getLoginScreen().remove(player);
        plugin.getWarpTransfer().warpToLobby(player);
    }
}
