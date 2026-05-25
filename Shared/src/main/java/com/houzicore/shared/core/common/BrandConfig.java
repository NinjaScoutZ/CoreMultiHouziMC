package com.houzicore.shared.core.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Central branding configuration loaded from houzicore-branding.properties.
 * All display names (server name, website, tab header, etc.) are read from this file
 * so they can be changed without recompiling.
 */
public class BrandConfig {

    public static String MainServerName;
    public static String MainUrlServer;

    private static final Properties PROPS = new Properties();
    private static boolean loaded = false;

    static {
        load();
    }

    public static void load() {
        Path configPath = Paths.get("houzicore-branding.properties");
        try (FileInputStream fis = new FileInputStream(configPath.toFile())) {
            PROPS.load(fis);
            loaded = true;
        } catch (IOException e) {
            loaded = false;
        }
        MainServerName = mainServerName();
        MainUrlServer = mainUrlServer();
    }

    public static String get(String key, String defaultValue) {
        return PROPS.getProperty(key, defaultValue);
    }

    // === Convenience getters ===

    public static String serverName() {
        return get("server.name", "HouziCore");
    }

    public static String mainServerName() {
        return serverName();
    }

    public static String networkName() {
        return get("server.network", serverName() + " Network");
    }

    public static String website() {
        return get("server.website", "houzicore.net");
    }

    public static String mainUrlServer() {
        return website();
    }

    public static String tabHeader() {
        return get("tab.header", "\u00a76\u00a7l" + serverName().toUpperCase());
    }

    public static String tabFooter() {
        return get("tab.footer", "\u00a78\u00a7m    \u00a7r \u00a76\u00a7lplay." + website() + " \u00a78\u00a7m    \u00a7r\n\u00a77Bilingual Minigames Network");
    }

    public static String welcomeTitle() {
        return get("welcome.title", "\u00a76\u00a7l" + serverName().toUpperCase());
    }

    public static String welcomeSubtitle(String playerName) {
        return get("welcome.subtitle", "\u00a77Welcome, %player%!").replace("%player%", playerName);
    }

    public static String welcomeScoreboard(String playerName) {
        return get("welcome.scoreboard", "Welcome %player%, to " + serverName() + "!").replace("%player%", playerName);
    }

    public static String purchaseUrl() {
        return get("purchase.url", "Contact an administrator");
    }

    public static String purchaseRank() {
        return get("purchase.rank", "Contact an administrator to obtain a rank!");
    }

    public static String appealsUrl() {
        return get("appeals.url", "Contact an administrator");
    }
}
