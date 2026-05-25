package com.houzicore.gateway;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

/**
 * Typed wrapper around config.yml values.
 */
public class GateConfig {

    private final JavaPlugin plugin;

    public GateConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // -----------------------------------------------------------------------
    // Database
    // -----------------------------------------------------------------------

    public String dbHost()     { return plugin.getConfig().getString("database.host",     "localhost"); }
    public int    dbPort()     { return plugin.getConfig().getInt("database.port",        3306); }
    public String dbName()     { return plugin.getConfig().getString("database.name",     "houzigate"); }
    public String dbUser()     { return plugin.getConfig().getString("database.user",     "root"); }
    public String dbPassword() { return plugin.getConfig().getString("database.password", ""); }
    public int    dbPoolSize() { return plugin.getConfig().getInt("database.pool_size",   5); }

    // -----------------------------------------------------------------------
    // Auth
    // -----------------------------------------------------------------------

    public int     minPasswordLength()    { return plugin.getConfig().getInt("auth.min_password_length", 6); }
    public int     maxAttempts()          { return plugin.getConfig().getInt("auth.max_attempts",        5); }
    public int     loginTimeoutSeconds()  { return plugin.getConfig().getInt("auth.login_timeout_seconds", 60); }
    public boolean premiumBypass()        { return plugin.getConfig().getBoolean("auth.premium_bypass",  true); }
    public int     maxTrustedIps()        { return plugin.getConfig().getInt("auth.max_trusted_ips",     10); }

    // -----------------------------------------------------------------------
    // Session & Security
    // -----------------------------------------------------------------------

    public boolean sessionLoginEnabled()       { return plugin.getConfig().getBoolean("session.enabled", true); }
    public int     sessionTimeoutMinutes()     { return plugin.getConfig().getInt("session.timeout_minutes", 15); }
    public boolean rateLimitEnabled()          { return plugin.getConfig().getBoolean("rate_limit.enabled", true); }
    public int     rateLimitMaxFailures()      { return plugin.getConfig().getInt("rate_limit.max_failures_per_ip", 10); }
    public int     rateLimitBanDurationMinutes() { return plugin.getConfig().getInt("rate_limit.ban_duration_minutes", 10); }
    public boolean uiRegisterConfirm()         { return plugin.getConfig().getBoolean("ui.register_confirm", true); }

    // -----------------------------------------------------------------------
    // 2FA
    // -----------------------------------------------------------------------

    public boolean twoFaEnabled()     { return plugin.getConfig().getBoolean("2fa.enabled",      true); }
    public int     minPinLength()     { return plugin.getConfig().getInt("2fa.min_pin_length",    4); }

    @SuppressWarnings("unchecked")
    public List<String> staffPermissions() {
        return plugin.getConfig().getStringList("2fa.staff_permissions");
    }

    // -----------------------------------------------------------------------
    // Bungee / Transfer
    // -----------------------------------------------------------------------

    /** BungeeCord server name to send players to after auth. */
    public String lobbyServer() {
        return plugin.getConfig().getString("bungee.lobby_server", "lobby");
    }

    /**
     * Display name used in warp status titles (supports &color codes).
     * Example: "&bLobby" → shown as blue "Lobby" in title.
     */
    public String lobbyDisplayName() {
        return color(plugin.getConfig().getString("bungee.lobby_display_name", "&bLobby"));
    }

    public String serverDisplayName() {
        String configured = plugin.getConfig().getString("server.name", null);
        if (configured != null && !configured.isBlank()) {
            return configured;
        }

        File brandingFile = new File("houzicore-branding.properties");
        if (brandingFile.exists()) {
            Properties props = new Properties();
            try (FileInputStream input = new FileInputStream(brandingFile)) {
                props.load(input);
                String branded = props.getProperty("server.name");
                if (branded != null && !branded.isBlank()) {
                    return branded;
                }
            } catch (Exception ignored) {
            }
        }

        return "Server";
    }

    // -----------------------------------------------------------------------
    // Screen
    // -----------------------------------------------------------------------

    /** Action bar prompt shown while player is waiting to login. */
    public String actionBarPrompt(Player player) {
        com.houzicore.gateway.auth.AuthSession s = GatewayPlugin.get().getSessionManager().get(player);
        if (s == null) return "";
        String key = switch (s.getState()) {
            case WAITING_REGISTER -> "screen.action_bar_register";
            case WAITING_2FA      -> "screen.action_bar_2fa";
            default               -> "screen.action_bar_login";
        };
        return color(plugin.getConfig().getString(key, "&e/login <password>"));
    }

    // -----------------------------------------------------------------------
    // Messages
    // -----------------------------------------------------------------------

    private String color(String s) {
        return s == null ? "" : org.bukkit.ChatColor.translateAlternateColorCodes('&', s);
    }

    private String msg(String key, String fallback) {
        return color(plugin.getConfig().getString("messages." + key, fallback));
    }

    public String prefix()           { return msg("prefix",           "&8[&bGate&8] "); }
    public String pleaseLogin()      { return msg("please_login",     "&ePlease type &a/login <password> &eto continue."); }
    public String pleaseRegister()   { return msg("please_register",  "&eWelcome! Type &a/register <pass> <pass>&e."); }
    public String please2fa()        { return msg("please_2fa",       "&eNew IP — type &a/2fa <pin>&e."); }
    public String loginSuccess()     { return msg("login_success",    "&aAuthenticated."); }
    public String registerSuccess()  { return msg("register_success", "&aAccount created."); }
    public String wrongPassword(int remaining) {
        return msg("wrong_password", "&cWrong password. &f<remaining> &cattempts left.")
                .replace("<remaining>", String.valueOf(remaining));
    }
    public String pinWrong()         { return msg("pin_wrong",        "&cIncorrect PIN."); }
    public String pinCorrect()       { return msg("pin_correct",      "&aIP verified."); }
    public String timeoutKick()      { return msg("timeout_kick",     "&cTimeout. Reconnect."); }
    public String maxAttemptsKick()  { return msg("max_attempts_kick","&cToo many attempts."); }
    public String alreadyAuthed()    { return msg("already_authed",   "&cAlready authenticated."); }
    public String notRegistered()    { return msg("not_registered",   "&cNo account. Use /register."); }
    public String registerMismatch() { return msg("register_mismatch","&cPasswords do not match."); }
    public String registerTooShort() {
        return msg("register_too_short", "&cMin length: &f<min>&c.")
                .replace("<min>", String.valueOf(minPasswordLength()));
    }
    public String pinSet()           { return msg("pin_set",          "&a2FA PIN updated."); }
    public String pinTooShort() {
        return msg("pin_too_short", "&cPIN min length: &f<min>&c.")
                .replace("<min>", String.valueOf(minPinLength()));
    }
    public String premiumDetected()  { return msg("premium_detected", "&bPremium account — password skipped."); }
    public String newIpNotice()      { return msg("new_ip_notice",    "&eLogging in from a new IP."); }
    public String ipBanned()         { return msg("ip_banned",        "&cIP temporarily blocked due to brute-force protection."); }
    public String registerConfirmPrompt() { return msg("register_confirm_prompt", "&ePlease confirm your password."); }
    public String registerConfirmMismatch() { return msg("register_confirm_mismatch", "&cPasswords do not match."); }

    public String adminReset(String player) {
        return msg("admin_reset", "&aPassword for &f<player> &areset.").replace("<player>", player);
    }
    public String adminTrust(String player, String ip) {
        return msg("admin_trust", "&aIP &f<ip> &atrusted for &f<player>&a.")
                .replace("<player>", player).replace("<ip>", ip);
    }
    public String adminClearTrust(String player) {
        return msg("admin_cleartrust", "&aCleared trusted IPs for &f<player>&a.").replace("<player>", player);
    }
    public String adminForceAuth(String player) {
        return msg("admin_forceauth", "&f<player> &aforce-authenticated.").replace("<player>", player);
    }
}
