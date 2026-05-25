package com.houzicore.shared.core.lang;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.houzicore.shared.core.preferences.PreferencesManager;
import com.houzicore.shared.core.preferences.UserPreferences;

/**
 * LangManager — centralizes all player-facing messages.
 *
 * PRIMARY SOURCE: Embedded Java classes (DefaultLang, DefaultLangTh).
 * These are compiled into the JAR and always available.
 *
 * OPTIONAL OVERLAY: If YAML files exist in plugins/HouziCore-Shared/,
 * their values will override the embedded defaults. This allows hot-fixing
 * text without recompiling.
 *
 * This architecture guarantees zero [Missing: ...] errors from deployment issues.
 */
public class LangManager {

    private static LangManager _instance;

    private final Plugin _plugin;
    private final PreferencesManager _prefManager;

    private final Map<String, Map<String, String>> _flatTranslationsMap = new HashMap<>();

    public LangManager(Plugin plugin, PreferencesManager prefManager) {
        _plugin      = plugin;
        _prefManager = prefManager;
        reload();
        _instance = this;
    }

    public static LangManager get() {
        return _instance;
    }

    /** Reload translations: Java defaults first, then YAML overlay. */
    public void reload() {
        // Step 1: Start with embedded Java defaults (always present)
        Map<String, String> flatEn = new HashMap<>(DefaultLang.DEFAULTS);
        Map<String, String> flatTh = new HashMap<>(DefaultLangTh.DEFAULTS);

        // Step 2: Overlay with bundled YAML catalogs packaged in the JAR
        overlayBundledYaml("messages_en.yml", flatEn);
        overlayBundledYaml("messages_th.yml", flatTh);
        overlayBundledYaml("messages/en/prop_rush.yml", flatEn);
        overlayBundledYaml("messages/th/prop_rush.yml", flatTh);
        overlayBundledYaml("messages/en/treasure.yml", flatEn);
        overlayBundledYaml("messages/th/treasure.yml", flatTh);
        overlayBundledYaml("messages/en/primal_games.yml", flatEn);
        overlayBundledYaml("messages/th/primal_games.yml", flatTh);
        overlayBundledYaml("messages/en/hideseek_kits.yml", flatEn);
        overlayBundledYaml("messages/th/hideseek_kits.yml", flatTh);
        overlayBundledYaml("messages/en/speedbuilders.yml", flatEn);
        overlayBundledYaml("messages/th/speedbuilders.yml", flatTh);

        // Step 3: Overlay with YAML files if they exist (optional hot-fix layer)
        overlayYaml("messages_en.yml", flatEn);
        overlayYaml("messages_th.yml", flatTh);

        // Step 4: Overlay with per-directory YAML files if they exist
        overlayDirectory("messages/en", flatEn);
        overlayDirectory("messages/th", flatTh);

        _flatTranslationsMap.clear();
        _flatTranslationsMap.put("ENG", flatEn);
        _flatTranslationsMap.put("THA", flatTh);
        _flatTranslationsMap.put("THAI", flatTh); // Alias
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /** Resolve message for a player using their language preference. */
    public String get(Player player, String key) {
        String lang = resolveLocaleStr(player);
        
        Map<String, String> flatMap = _flatTranslationsMap.get(lang);
        if (flatMap == null) flatMap = _flatTranslationsMap.get("ENG");
        
        String value = flatMap.get(key);
        // Fallback to English if the key is missing in the player's language
        if (value == null) {
            value = _flatTranslationsMap.get("ENG").get(key);
        }
        if (value == null) {
            value = "§c[Missing: " + key + "]";
        }
        return colorize(value);
    }

    /** Resolve message for a player using their language preference, returning a fallback if missing. */
    public String getOrDefault(Player player, String key, String fallback) {
        String lang = resolveLocaleStr(player);
        
        Map<String, String> flatMap = _flatTranslationsMap.get(lang);
        if (flatMap == null) flatMap = _flatTranslationsMap.get("ENG");
        
        String value = flatMap.get(key);
        if (value == null) {
            value = _flatTranslationsMap.get("ENG").get(key);
        }
        if (value == null) {
            value = fallback;
        }
        return colorize(value);
    }

    /** Message with {0} … {N} placeholder substitution. */
    public String get(Player player, String key, Object... args) {
        String raw = get(player, key);
        
        // Safety check for nested arrays (in case caller passes Object[] directly into varargs)
        if (args != null && args.length == 1 && args[0] instanceof Object[]) {
            args = (Object[]) args[0];
        }
        
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                String target = "{" + i + "}";
                if (raw.contains(target)) {
                    raw = raw.replace(target, String.valueOf(args[i]));
                }
            }
        }
        return raw;
    }

    /** Server-side / admin messages — always English. */
    public String get(String key) {
        Map<String, String> flatMap = _flatTranslationsMap.get("ENG");
        String value = flatMap != null ? flatMap.get(key) : null;
        if (value == null) {
            value = "§c[Missing: " + key + "]";
        }
        return colorize(value);
    }

    /** Admin message with fallback. */
    public String getOrDefault(String key, String fallback) {
        Map<String, String> flatMap = _flatTranslationsMap.get("ENG");
        String value = flatMap != null ? flatMap.get(key) : null;
        if (value == null) {
            value = fallback;
        }
        return colorize(value);
    }

    /** Admin message with placeholders. */
    public String get(String key, Object... args) {
        String raw = get(key);
        
        if (args != null && args.length == 1 && args[0] instanceof Object[]) {
            args = (Object[]) args[0];
        }
        
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                String target = "{" + i + "}";
                if (raw.contains(target)) {
                    raw = raw.replace(target, String.valueOf(args[i]));
                }
            }
        }
        return raw;
    }

    /** Retrieve flat translation mappings for a language. */
    public Map<String, String> flat(String langCode) {
        Map<String, String> map = _flatTranslationsMap.get(langCode.toUpperCase());
        return map != null ? map : _flatTranslationsMap.get("ENG");
    }

    /** Resolves the system's preferred language string for a given player. */
    public String resolveLocaleStr(Player player) {
        if (_prefManager != null && player != null) {
            UserPreferences prefs = _prefManager.Get(player.getName());
            if (prefs != null && prefs.Language != null) {
                return prefs.Language.toUpperCase();
            }
        }
        // Native fallback (e.g. en_us) converted to our format could go here.
        return "ENG";
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * If a YAML file exists in the shared data folder, overlay its values
     * onto the given map. Does NOT create or copy YAML files — they are
     * purely optional overrides on top of the embedded Java defaults.
     */
    private void overlayYaml(String name, Map<String, String> map) {
        File pluginsFolder = _plugin.getDataFolder().getParentFile();
        File dataFolder = new File(pluginsFolder, "HouziCore-Shared");
        File file = new File(dataFolder, name);
        if (!file.exists()) return; // No YAML override — Java defaults are fine

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        flattenConfigs(cfg, map);
    }

    private void overlayBundledYaml(String resourcePath, Map<String, String> map) {
        try (InputStream in = openBundledResource(resourcePath)) {
            if (in == null) return;

            InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
            FileConfiguration cfg = YamlConfiguration.loadConfiguration(reader);
            flattenConfigs(cfg, map);
        }
        catch (Exception ignored) {
        }
    }

    private InputStream openBundledResource(String resourcePath) {
        if (_plugin != null) {
            InputStream pluginStream = _plugin.getResource(resourcePath);
            if (pluginStream != null) {
                return pluginStream;
            }
        }

        ClassLoader sharedLoader = LangManager.class.getClassLoader();
        if (sharedLoader != null) {
            InputStream sharedStream = sharedLoader.getResourceAsStream(resourcePath);
            if (sharedStream != null) {
                return sharedStream;
            }
        }

        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        if (contextLoader != null) {
            return contextLoader.getResourceAsStream(resourcePath);
        }

        return null;
    }

    private void overlayDirectory(String path, Map<String, String> map) {
        File pluginsFolder = _plugin.getDataFolder().getParentFile();
        File dataFolder = new File(pluginsFolder, "HouziCore-Shared");
        File dir = new File(dataFolder, path);
        if (!dir.exists()) return; // No directory — Java defaults are fine

        File[] files = dir.listFiles((d, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File f : files) {
            FileConfiguration cfg = YamlConfiguration.loadConfiguration(f);
            flattenConfigs(cfg, map);
        }
    }

    private void flattenConfigs(FileConfiguration cfg, Map<String, String> map) {
        if (cfg == null) return;
        for (String key : cfg.getKeys(true)) {
            if (cfg.isString(key)) {
                map.put(key, cfg.getString(key));
            } else if (cfg.isList(key)) {
                java.util.List<String> list = cfg.getStringList(key);
                if (!list.isEmpty()) {
                    map.put(key, String.join("\n", list));
                }
            }
        }
    }

    private String colorize(String s) {
        return com.houzicore.shared.common.util.HouziColorParser.parse(s.replace("&", "§"));
    }

    /** Returns true if the given player has their language set to Thai. */
    public boolean isThai(org.bukkit.entity.Player player) {
        if (player == null || _prefManager == null) return false;
        com.houzicore.shared.core.preferences.UserPreferences prefs = _prefManager.Get(player.getName());
        if (prefs == null || prefs.Language == null) return false;
        String lang = prefs.Language.toUpperCase();
        return "THA".equals(lang) || "THAI".equals(lang);
    }
}
