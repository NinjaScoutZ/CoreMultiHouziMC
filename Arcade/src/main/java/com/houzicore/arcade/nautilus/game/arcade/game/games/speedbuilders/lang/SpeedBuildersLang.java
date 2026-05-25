package com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.lang;

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

public final class SpeedBuildersLang {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private static SpeedBuildersLang INSTANCE;

    private final MiniMessage mini;

    private SpeedBuildersLang() {
        this.mini = MiniMessage.miniMessage();
    }

    public static synchronized void init(Plugin plugin) {
        if (INSTANCE == null) {
            INSTANCE = new SpeedBuildersLang();
        }
    }

    public static synchronized SpeedBuildersLang ensureInitialized(Plugin plugin) {
        if (INSTANCE == null) {
            init(plugin);
        }
        return INSTANCE;
    }

    public static synchronized SpeedBuildersLang get() {
        if (INSTANCE == null) {
            INSTANCE = new SpeedBuildersLang();
        }
        return INSTANCE;
    }

    public Locale resolveLocale(Player player) {
        String lang = resolveLocaleStr(player);
        if ("THA".equals(lang) || "THAI".equals(lang)) {
            return Locale.forLanguageTag("th-TH");
        }
        return Locale.ENGLISH;
    }

    public String resolveLocaleStr(Player player) {
        if (LangManager.get() != null) {
            return LangManager.get().resolveLocaleStr(player);
        }
        return "ENG";
    }

    public String get(Player player, String key, Object... args) {
        if (LangManager.get() != null) {
            return LangManager.get().get(player, key, args);
        }
        return "§c[Missing: " + key + "]";
    }

    public String[] list(Player player, String key, String[] fallback) {
        String raw = lookupWithFallback(player, key);
        if (raw == null) {
            return fallback;
        }

        String[] lines = raw.split("\\r?\\n", -1);
        String[] rendered = new String[lines.length];

        for (int i = 0; i < lines.length; i++) {
            rendered[i] = LEGACY.serialize(deserialize(lines[i]));
        }

        return rendered;
    }

    public Component component(Player player, String key, TagResolver... tags) {
        String raw = lookupWithFallback(player, key);
        if (raw == null) {
            raw = "<red>[Missing: " + key + "]";
        }
        return deserialize(raw, tags);
    }

    private String lookupWithFallback(Player player, String key) {
        Map<String, String> localized = LangManager.get() != null ? LangManager.get().flat(resolveLocaleStr(player)) : null;
        if (localized != null && localized.containsKey(key)) {
            return localized.get(key);
        }

        Map<String, String> english = LangManager.get() != null ? LangManager.get().flat("ENG") : null;
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

    @Deprecated
    public String getString(Player player, String key, String fallback) {
        String raw = lookupWithFallback(player, key);
        if (raw == null) {
            return colorize(fallback);
        }
        return LEGACY.serialize(deserialize(raw));
    }

    @Deprecated
    public String[] getStringList(Player player, String key, String[] fallback) {
        return list(player, key, fallback);
    }

    @Deprecated
    public String getString(Player player, String key) {
        return getString(player, key, "");
    }

    private String colorize(String text) {
        if (text == null) {
            return null;
        }
        return HouziColorParser.parse(text.replace("&", "§"));
    }
}
