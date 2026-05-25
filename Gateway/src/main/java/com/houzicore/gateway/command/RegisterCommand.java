package com.houzicore.gateway.command;

import com.houzicore.gateway.GatewayPlugin;
import com.houzicore.gateway.auth.AuthSession;
import com.houzicore.gateway.auth.AuthSession.State;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mindrot.jbcrypt.BCrypt;

public class RegisterCommand implements CommandExecutor {

    private final GatewayPlugin plugin;

    public RegisterCommand(GatewayPlugin plugin) {
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
        if (session.getState() == State.WAITING_LOGIN) {
            player.sendMessage(prefix + plugin.getGateConfig().pleaseLogin());
            return true;
        }
        if (args.length < 2) {
            player.sendMessage(prefix + "§cUsage: /register <password> <confirm>");
            return true;
        }

        String pass1 = args[0];
        String pass2 = args[1];

        if (!pass1.equals(pass2)) {
            player.sendMessage(prefix + plugin.getGateConfig().registerMismatch());
            return true;
        }
        if (pass1.length() < plugin.getGateConfig().minPasswordLength()) {
            player.sendMessage(prefix + plugin.getGateConfig().registerTooShort());
            return true;
        }

        String name = player.getName();
        if (plugin.getDatabase().accountExists(name)) {
            session.setState(State.WAITING_LOGIN);
            player.sendMessage(prefix + plugin.getGateConfig().pleaseLogin());
            return true;
        }

        // Hash and store
        String hash = BCrypt.hashpw(pass1, BCrypt.gensalt(10));
        plugin.getDatabase().createAccount(name, hash, false);

        // Trust first IP
        String ip = session.getIp();
        plugin.getIpTrustManager().trust(name, ip);

        // Authenticate + remove screen + warp
        player.sendMessage(prefix + plugin.getGateConfig().registerSuccess());
        plugin.getSessionManager().markAuthenticated(player);
        plugin.getDatabase().updateLastLogin(name, ip);

        plugin.getLoginScreen().remove(player);
        plugin.getWarpTransfer().warpToLobby(player);
        return true;
    }
}
