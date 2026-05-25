package com.houzicore.arcade.nautilus.game.arcade.kit;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class KitConfigLoader {

    public static class KitConfigData {
        public String name;
        public KitAvailability availability;
        public int cost;
        public EntityType entityType;
        public Material displayItem;
        public String[] descEn;
        public String[] descTh;

        public boolean isValid() {
            return name != null;
        }
    }

    public static KitConfigData loadKitData(JavaPlugin plugin, String fileName, String kitKey, KitConfigData fallback) {
        try {
            InputStream in = plugin.getResource("kits/" + fileName);
            if (in == null) return fallback;
            
            YamlConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
            ConfigurationSection section = config.getConfigurationSection(kitKey);
            
            if (section == null) return fallback;
            
            KitConfigData data = new KitConfigData();
            data.name = section.getString("name", fallback.name);
            data.availability = KitAvailability.valueOf(section.getString("availability", fallback.availability.name()));
            data.cost = section.getInt("cost", fallback.cost);
            data.entityType = EntityType.valueOf(section.getString("entity-type", fallback.entityType.name()));
            data.displayItem = Material.valueOf(section.getString("display-item", fallback.displayItem.name()));
            
            if (section.contains("desc-en")) {
                data.descEn = section.getStringList("desc-en").toArray(new String[0]);
            } else {
                data.descEn = fallback.descEn;
            }
            
            if (section.contains("desc-th")) {
                data.descTh = section.getStringList("desc-th").toArray(new String[0]);
            } else {
                data.descTh = fallback.descTh;
            }
            
            return data;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load kit config for " + kitKey + " from " + fileName + ": " + e.getMessage());
            return fallback;
        }
    }
}
