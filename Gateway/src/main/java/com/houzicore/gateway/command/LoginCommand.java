package com.houzicore.gateway.command;

import com.houzicore.gateway.GatewayPlugin;
import com.houzicore.gateway.auth.AuthSession;
import com.houzicore.gateway.auth.AuthSession.State;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mindrot.jbcrypt.BCrypt;

public class LoginCommand implements CommandExecutor {

    private final GatewayPlugin plugin;

    public LoginCommand(GatewayPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        String prefix = plugin.getGateConfig().prefix();
        AuthSession session = plugin.getSessionManager().get(player);

        if (session == null || session.isAuthenticated()) {
            player.sendMessage(prefix + plugin.getGateConfig().alreadyAuthed());
            return true;
        }
        if (session.getState() == State.WAITING_REGISTER) {
            player.sendMessage(prefix + plugin.getGateConfig().pleaseRegister());
            return true;
        }
        if (session.getState() == State.WAITING_2FA) {
            player.sendMessage(prefix + plugin.getGateConfig().please2fa());
            return true;
        }
        if (args.length < 1) {
            player.sendMessage(prefix + "§cUsage: /login <password>");
            return true;
        }

        String name    = player.getName();
        String rawPass = args[0];
        String hash    = plugin.getDatabase().getPasswordHash(name);

        if (hash == null) {
            player.sendMessage(prefix + plugin.getGateConfig().notRegistered());
            session.setState(State.WAITING_REGISTER);
            return true;
        }

        if (!BCrypt.checkpw(rawPass, hash)) {
            session.incrementFailed();
            int remaining = plugin.getGateConfig().maxAttempts() - session.getFailedAttempts();
            if (remaining <= 0) {
                player.kickPlayer(plugin.getGateConfig().maxAttemptsKick());
                return true;
            }
            player.sendMessage(prefix + plugin.getGateConfig().wrongPassword(remaining));
            return true;
        }

        // Password correct — check IP / 2FA
        String ip     = session.getIp();
        boolean trusted = plugin.getIpTrustManager().isTrusted(name, ip);

        if (!trusted && plugin.getGateConfig().twoFaEnabled()
                     && plugin.getTwoFactorManager().hasPin(name)) {
            session.setState(State.WAITING_2FA);
            player.sendMessage(prefix + plugin.getGateConfig().newIpNotice());
            player.sendMessage(prefix + plugin.getGateConfig().please2fa());
            return true;
        }

        // Fully authenticated — remove screen and warp
        completeAuth(player, session);
        return true;
    }

    // -----------------------------------------------------------------------

    static void completeAuth(Player player, AuthSession session) {
        GatewayPlugin plugin = GatewayPlugin.get();
        String ip = session.getIp();

        player.sendMessage(plugin.getGateConfig().prefix() + plugin.getGateConfig().loginSuccess());
        plugin.getSessionManager().markAuthenticated(player);
        plugin.getDatabase().updateLastLogin(player.getName(), ip);

        if (!plugin.getIpTrustManager().isTrusted(player.getName(), ip)) {
            plugin.getIpTrustManager().trust(player.getName(), ip);
        }

        // Remove blindness + start cinematic warp → Lobby
        plugin.getLoginScreen().remove(player);
        plugin.getWarpTransfer().warpToLobby(player);
    }
}
