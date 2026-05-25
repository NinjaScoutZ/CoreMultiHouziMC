package com.houzicore.gateway.command;

import com.houzicore.gateway.GatewayPlugin;
import com.houzicore.gateway.auth.AuthSession;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /gateadmin <subcommand> [args]
 *
 *   resetpass <player>       — delete account (force re-register)
 *   trustip <player> <ip>    — manually add trusted IP
 *   cleartrust <player>      — remove all trusted IPs
 *   forceauth <player>       — force-authenticate a player (testing)
 *   listips <player>         — show trusted IPs
 */
public class GateAdminCommand implements CommandExecutor {

    private final GatewayPlugin plugin;

    public GateAdminCommand(GatewayPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("houzigate.admin")) {
            sender.sendMessage("§cNo permission.");
            return true;
        }

        String prefix = plugin.getGateConfig().prefix();

        if (args.length < 1) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "resetpass"  -> handleResetPass(sender, prefix, args);
            case "trustip"    -> handleTrustIp(sender, prefix, args);
            case "cleartrust" -> handleClearTrust(sender, prefix, args);
            case "forceauth"  -> handleForceAuth(sender, prefix, args);
            case "listips"    -> handleListIps(sender, prefix, args);
            default           -> sendHelp(sender);
        }
        return true;
    }

    // -----------------------------------------------------------------------
    // Subcommands
    // -----------------------------------------------------------------------

    private void handleResetPass(CommandSender sender, String prefix, String[] args) {
        if (args.length < 2) { sender.sendMessage(prefix + "§cUsage: /gateadmin resetpass <player>"); return; }
        String name = args[1];
        plugin.getDatabase().resetPassword(name);
        plugin.getDatabase().clearTrustedIps(name);
        sender.sendMessage(prefix + plugin.getGateConfig().adminReset(name));
    }

    private void handleTrustIp(CommandSender sender, String prefix, String[] args) {
        if (args.length < 3) { sender.sendMessage(prefix + "§cUsage: /gateadmin trustip <player> <ip>"); return; }
        String name = args[1];
        String ip   = args[2];
        plugin.getIpTrustManager().trust(name, ip);
        sender.sendMessage(prefix + plugin.getGateConfig().adminTrust(name, ip));
    }

    private void handleClearTrust(CommandSender sender, String prefix, String[] args) {
        if (args.length < 2) { sender.sendMessage(prefix + "§cUsage: /gateadmin cleartrust <player>"); return; }
        String name = args[1];
        plugin.getIpTrustManager().clearAll(name);
        sender.sendMessage(prefix + plugin.getGateConfig().adminClearTrust(name));
    }

    private void handleForceAuth(CommandSender sender, String prefix, String[] args) {
        if (args.length < 2) { sender.sendMessage(prefix + "§cUsage: /gateadmin forceauth <player>"); return; }
        String name   = args[1];
        Player target = Bukkit.getPlayerExact(name);

        if (target == null) {
            sender.sendMessage(prefix + "§cPlayer not online.");
            return;
        }

        plugin.getSessionManager().markAuthenticated(target);
        plugin.getLoginScreen().remove(target);
        plugin.getWarpTransfer().warpToLobby(target);
        sender.sendMessage(prefix + plugin.getGateConfig().adminForceAuth(name));
        target.sendMessage(prefix + "§aYou have been force-authenticated by an admin.");
    }

    private void handleListIps(CommandSender sender, String prefix, String[] args) {
        if (args.length < 2) { sender.sendMessage(prefix + "§cUsage: /gateadmin listips <player>"); return; }
        String name = args[1];
        var ips = plugin.getDatabase().getTrustedIps(name);

        if (ips.isEmpty()) {
            sender.sendMessage(prefix + "§cNo trusted IPs for §f" + name + "§c.");
            return;
        }

        sender.sendMessage(prefix + "§eTrusted IPs for §f" + name + "§e:");
        for (String ip : ips) {
            sender.sendMessage("  §7• §f" + ip);
        }
    }

    // -----------------------------------------------------------------------
    // Help
    // -----------------------------------------------------------------------

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§b§lHouziGate Admin Commands:");
        sender.sendMessage("§7  /gateadmin resetpass §f<player>");
        sender.sendMessage("§7  /gateadmin trustip §f<player> <ip>");
        sender.sendMessage("§7  /gateadmin cleartrust §f<player>");
        sender.sendMessage("§7  /gateadmin forceauth §f<player>");
        sender.sendMessage("§7  /gateadmin listips §f<player>");
    }
}
