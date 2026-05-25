package com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.lang;

import java.util.Locale;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.houzicore.shared.common.util.HouziColorParser;
import com.houzicore.shared.core.lang.LangManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Locale-aware facade for Survival Primal Games.
 * All player-facing text should go through this class.
 * 
 * Keys are stored in:
 *   Code/Shared/src/main/resources/messages/en/primal_games.yml
 *   Code/Shared/src/main/resources/messages/th/primal_games.yml
 */
public final class PrimalGamesLang {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    private static PrimalGamesLang INSTANCE;

    private final MiniMessage mini;

    private PrimalGamesLang() {
        this.mini = MiniMessage.miniMessage();
    }

    public static synchronized void init(Plugin plugin) {
        if (INSTANCE == null) {
            INSTANCE = new PrimalGamesLang();
        }
    }

    public static synchronized PrimalGamesLang ensureInitialized(Plugin plugin) {
        if (INSTANCE == null) {
            init(plugin);
        }
        return INSTANCE;
    }

    public static synchronized PrimalGamesLang get() {
        if (INSTANCE == null) {
            INSTANCE = new PrimalGamesLang();
        }
        return INSTANCE;
    }

    // ─────────────────────────────────────────────
    // Locale resolution
    // ─────────────────────────────────────────────

    public Locale resolveLocale(Player player) {
        String lang = resolveLocaleStr(player);
        if ("THA".equals(lang) || "THAI".equals(lang)) {
            return Locale.forLanguageTag("th-TH");
        }
        return Locale.ENGLISH;
    }

    public String resolveLocaleStr(Player player) {
        if (player == null) return "ENG";
        if (LangManager.get() != null) {
            return LangManager.get().resolveLocaleStr(player);
        }
        return "ENG";
    }

    public boolean isThai(Player player) {
        if (LangManager.get() != null) {
            return LangManager.get().isThai(player);
        }
        return false;
    }

    // ─────────────────────────────────────────────
    // Core lookup methods
    // ─────────────────────────────────────────────

    /**
     * Get a localized string for the given key, rendered as a legacy §-coded string.
     */
    public String get(Player player, String key) {
        return getFallback(player, key);
    }

    /**
     * Get a localized string with placeholder replacement.
     * Placeholders use {key} format — replaced before MiniMessage parsing.
     */
    public String get(Player player, String key, String... replacements) {
        String raw = lookupWithFallback(player, key);
        if (raw == null) {
            raw = "§c[Missing: " + key + "]";
        }

        // Replace {key} style placeholders in pairs: key1, value1, key2, value2, ...
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            raw = raw.replace("{" + replacements[i] + "}", replacements[i + 1]);
        }

        return colorize(raw);
    }

    /**
     * Get a localized string list (for multi-line descriptions).
     */
    public String[] list(Player player, String key, String[] fallback) {
        String raw = lookupWithFallback(player, key);
        if (raw == null) {
            return fallback;
        }

        String[] lines = raw.split("\\r?\\n", -1);
        String[] rendered = new String[lines.length];

        for (int i = 0; i < lines.length; i++) {
            rendered[i] = colorize(lines[i]);
        }

        return rendered;
    }

    /**
     * Get a localized Adventure Component.
     */
    public Component component(Player player, String key, TagResolver... tags) {
        String raw = lookupWithFallback(player, key);
        if (raw == null) {
            raw = "<red>[Missing: " + key + "]";
        }
        return deserialize(raw, tags);
    }

    // ─────────────────────────────────────────────
    // Convenience methods for common patterns
    // ─────────────────────────────────────────────

    /**
     * Build a BossBar title string for the current border state.
     */
    public String bossBar(Player player, String phaseKey, String size, String time) {
        String raw = lookupWithFallback(player, "primal_games.bossbar." + phaseKey);
        if (raw == null) return "§c[Missing bossbar." + phaseKey + "]";
        raw = raw.replace("{size}", size);
        if (time != null) raw = raw.replace("{time}", time);
        return colorize(raw);
    }

    /**
     * Build an ActionBar string for the current game state.
     */
    public String actionBar(Player player, String stateKey, int kills, int alive, String size) {
        String raw = lookupWithFallback(player, "primal_games.actionbar." + stateKey);
        if (raw == null) return "§c[Missing actionbar." + stateKey + "]";
        raw = raw.replace("{kills}", String.valueOf(kills))
                 .replace("{alive}", String.valueOf(alive))
                 .replace("{size}", size != null ? size : "?");
        return colorize(raw);
    }

    /**
     * Build a death title.
     */
    public String deathTitle(Player player) {
        return get(player, "primal_games.title.death");
    }

    /**
     * Build a death subtitle with killer name.
     */
    public String deathSubtitle(Player player, String killerName) {
        if (killerName == null) return "";
        return get(player, "primal_games.title.death_killed_by", "killer", killerName);
    }

    /**
     * Build the game-started announcement header.
     */
    public String announceHeader() {
        String raw = lookupWithFallback(null, "primal_games.announce.header");
        if (raw == null) {
            raw = "§c§l⚡ ꜱᴜʀᴠɪᴠᴀʟ ɢᴀᴍᴇꜱ §8| §f";
        }
        return colorize(raw);
    }

    // ─────────────────────────────────────────────
    // Internal lookup
    // ─────────────────────────────────────────────

    private String getFallback(Player player, String key) {
        String raw = lookupWithFallback(player, key);
        if (raw == null) {
            raw = "§c[Missing: " + key + "]";
        }
        return colorize(raw);
    }

    private String lookupWithFallback(Player player, String key) {
        Map<String, String> localized = LangManager.get() != null
                ? LangManager.get().flat(resolveLocaleStr(player)) : null;
        if (localized != null && localized.containsKey(key)) {
            return localized.get(key);
        }

        Map<String, String> english = LangManager.get() != null
                ? LangManager.get().flat("ENG") : null;
        if (english != null && english.containsKey(key)) {
            return english.get(key);
        }

        return null;
    }

    private Component deserialize(String raw, TagResolver... tags) {
        return mini.deserialize(normalizeForMiniMessage(raw), tags);
    }

    private String normalizeForMiniMessage(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }

        String source = raw.replace('§', '&');
        StringBuilder out = new StringBuilder(source.length() + 32);

        for (int i = 0; i < source.length(); i++) {
            char ch = source.charAt(i);
            if (ch == '&' && i + 1 < source.length()) {
                String tag = toMiniTag(source.charAt(i + 1));
                if (tag != null) {
                    out.append(tag);
                    i++;
                    continue;
                }
            }
            out.append(ch);
        }

        return out.toString();
    }

    private String toMiniTag(char code) {
        return switch (Character.toLowerCase(code)) {
            case '0' -> "<black>";
            case '1' -> "<dark_blue>";
            case '2' -> "<dark_green>";
            case '3' -> "<dark_aqua>";
            case '4' -> "<dark_red>";
            case '5' -> "<dark_purple>";
            case '6' -> "<gold>";
            case '7' -> "<gray>";
            case '8' -> "<dark_gray>";
            case '9' -> "<blue>";
            case 'a' -> "<green>";
            case 'b' -> "<aqua>";
            case 'c' -> "<red>";
            case 'd' -> "<light_purple>";
            case 'e' -> "<yellow>";
            case 'f' -> "<white>";
            case 'k' -> "<obfuscated>";
            case 'l' -> "<bold>";
            case 'm' -> "<strikethrough>";
            case 'n' -> "<underlined>";
            case 'o' -> "<italic>";
            case 'r' -> "<reset>";
            default -> null;
        };
    }

    private String colorize(String text) {
        if (text == null) {
            return null;
        }
        return HouziColorParser.parse(text.replace("&", "§"));
    }
}
