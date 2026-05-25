package com.houzicore.gateway.command;

import com.houzicore.gateway.GatewayPlugin;
import com.houzicore.gateway.auth.AuthSession;
import com.houzicore.gateway.auth.AuthSession.State;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Internal command: /hzgate-submit <password>
 *
 * Fired automatically by the Dialog API's commandTemplate when the player
 * clicks the submit button. Not intended to be run manually by players.
 *
 * Handles all states: LOGIN, REGISTER, REGISTER_CONFIRM, 2FA.
 */
public class HzGateSubmitCommand implements CommandExecutor {

    private final GatewayPlugin plugin;

    public HzGateSubmitCommand(GatewayPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (args.length == 0) return true;

        String typed = String.join(" ", args);

        AuthSession session = plugin.getSessionManager().get(player);
        if (session == null || session.isAuthenticated()) return true;

        String prefix = plugin.getGateConfig().prefix();
        String name   = player.getName();
        String ip     = session.getIp();

        // 1. IP brute-force protection check
        if (plugin.getRateLimiter().isBanned(ip)) {
            player.kickPlayer(plugin.getGateConfig().ipBanned());
            return true;
        }

        switch (session.getState()) {

            case WAITING_LOGIN -> {
                String hash = plugin.getDatabase().getPasswordHash(name);
                if (hash == null) {
                    // Account not found → switch to register
                    session.setState(State.WAITING_REGISTER);
                    String msg = plugin.getGateConfig().notRegistered();
                    player.sendMessage(prefix + msg);
                    plugin.getDialogLoginUI().reopen(player, msg);
                    return true;
                }
                if (!BCrypt.checkpw(typed, hash)) {
                    plugin.getRateLimiter().recordFailure(ip);
                    if (plugin.getRateLimiter().isBanned(ip)) {
                        player.kickPlayer(plugin.getGateConfig().ipBanned());
                        plugin.getDatabase().logLogin(name, ip, "BANNED_BRUTEFORCE");
                        return true;
                    }
                    session.incrementFailed();
                    int left = plugin.getGateConfig().maxAttempts() - session.getFailedAttempts();
                    if (left <= 0) {
                        player.kickPlayer(plugin.getGateConfig().maxAttemptsKick());
                        plugin.getDatabase().logLogin(name, ip, "KICK_MAX_ATTEMPTS");
                        return true;
                    }
                    String msg = plugin.getGateConfig().wrongPassword(left);
                    player.sendMessage(prefix + msg);
                    plugin.getDialogLoginUI().reopen(player, msg);
                    return true;
                }
                
                // Password correct
                plugin.getRateLimiter().clear(ip);
                session.resetFailedAttempts();

                // Check 2FA
                boolean trusted = plugin.getIpTrustManager().isTrusted(name, ip);
                if (!trusted && plugin.getGateConfig().twoFaEnabled()
                             && plugin.getDatabase().hasPin(name)) {
                    session.setState(State.WAITING_2FA);
                    String msg = plugin.getGateConfig().newIpNotice();
                    player.sendMessage(prefix + msg);
                    plugin.getDialogLoginUI().reopen(player, msg);
                    return true;
                }
                
                // Complete
                plugin.getDatabase().logLogin(name, ip, "LOGIN_SUCCESS");
                completeAuth(player, session);
            }

            case WAITING_REGISTER -> {
                if (typed.length() < plugin.getGateConfig().minPasswordLength()) {
                    String msg = plugin.getGateConfig().registerTooShort();
                    player.sendMessage(prefix + msg);
                    plugin.getDialogLoginUI().reopen(player, msg);
                    return true;
                }
                if (plugin.getDatabase().accountExists(name)) {
                    // Race: account created between sessions — go to login
                    session.setState(State.WAITING_LOGIN);
                    String msg = plugin.getGateConfig().pleaseLogin();
                    player.sendMessage(prefix + msg);
                    plugin.getDialogLoginUI().reopen(player, msg);
                    return true;
                }

                if (plugin.getGateConfig().uiRegisterConfirm()) {
                    // Store the first draft and move to confirmation screen
                    session.setPasswordDraft(typed);
                    session.setState(State.WAITING_REGISTER_CONFIRM);
                    String msg = plugin.getGateConfig().registerConfirmPrompt();
                    player.sendMessage(prefix + msg);
                    plugin.getDialogLoginUI().reopen(player, msg);
                } else {
                    // Register immediately if confirmation is disabled
                    String hash = BCrypt.hashpw(typed, BCrypt.gensalt(10));
                    plugin.getDatabase().createAccount(name, hash, false);
                    plugin.getDatabase().logLogin(name, ip, "REGISTER_SUCCESS");
                    
                    plugin.getIpTrustManager().trust(name, ip);
                    plugin.getDatabase().updateLastLogin(name, ip);
                    player.sendMessage(prefix + plugin.getGateConfig().registerSuccess());
                    plugin.getSessionManager().markAuthenticated(player);
                    
                    // Create session
                    plugin.getSessionLoginManager().createSession(name, ip);
                    
                    plugin.getLoginScreen().remove(player);
                    plugin.getWarpTransfer().warpToLobby(player);
                }
            }

            case WAITING_REGISTER_CONFIRM -> {
                String draft = session.getPasswordDraft();
                if (draft == null || !draft.equals(typed)) {
                    // Reset and send back to step 1
                    session.setPasswordDraft(null);
                    session.setState(State.WAITING_REGISTER);
                    String msg = plugin.getGateConfig().registerConfirmMismatch();
                    player.sendMessage(prefix + msg);
                    plugin.getDialogLoginUI().reopen(player, msg);
                    return true;
                }

                // Match! Save account
                session.setPasswordDraft(null);
                String hash = BCrypt.hashpw(typed, BCrypt.gensalt(10));
                plugin.getDatabase().createAccount(name, hash, false);
                plugin.getDatabase().logLogin(name, ip, "REGISTER_SUCCESS");

                plugin.getIpTrustManager().trust(name, ip);
                plugin.getDatabase().updateLastLogin(name, ip);
                player.sendMessage(prefix + plugin.getGateConfig().registerSuccess());
                plugin.getSessionManager().markAuthenticated(player);

                // Create session
                plugin.getSessionLoginManager().createSession(name, ip);

                plugin.getLoginScreen().remove(player);
                plugin.getWarpTransfer().warpToLobby(player);
            }

            case WAITING_2FA -> {
                String correctPinHash = plugin.getDatabase().getPinHash(name);
                if (correctPinHash == null || !BCrypt.checkpw(typed, correctPinHash)) {
                    plugin.getRateLimiter().recordFailure(ip);
                    if (plugin.getRateLimiter().isBanned(ip)) {
                        player.kickPlayer(plugin.getGateConfig().ipBanned());
                        plugin.getDatabase().logLogin(name, ip, "BANNED_BRUTEFORCE_2FA");
                        return true;
                    }
                    session.incrementFailed();
                    int left = plugin.getGateConfig().maxAttempts() - session.getFailedAttempts();
                    if (left <= 0) {
                        player.kickPlayer(plugin.getGateConfig().maxAttemptsKick());
                        plugin.getDatabase().logLogin(name, ip, "KICK_MAX_ATTEMPTS_2FA");
                        return true;
                    }
                    String msg = plugin.getGateConfig().pinWrong();
                    player.sendMessage(prefix + msg);
                    plugin.getDialogLoginUI().reopen(player, msg);
                    return true;
                }

                // PIN correct
                plugin.getRateLimiter().clear(ip);
                session.resetFailedAttempts();
                
                player.sendMessage(prefix + plugin.getGateConfig().pinCorrect());
                plugin.getDatabase().logLogin(name, ip, "2FA_SUCCESS");
                completeAuth(player, session);
            }

            default -> plugin.getDialogLoginUI().reopen(player, null);
        }
        return true;
    }

    private void completeAuth(Player player, AuthSession session) {
        String ip     = session.getIp();
        String name   = player.getName();
        String prefix = plugin.getGateConfig().prefix();
        
        player.sendMessage(prefix + plugin.getGateConfig().loginSuccess());
        plugin.getSessionManager().markAuthenticated(player);
        plugin.getDatabase().updateLastLogin(name, ip);
        
        if (!plugin.getIpTrustManager().isTrusted(name, ip)) {
            plugin.getIpTrustManager().trust(name, ip);
        }
        
        // Create session
        plugin.getSessionLoginManager().createSession(name, ip);
        
        plugin.getLoginScreen().remove(player);
        plugin.getWarpTransfer().warpToLobby(player);
    }
}
