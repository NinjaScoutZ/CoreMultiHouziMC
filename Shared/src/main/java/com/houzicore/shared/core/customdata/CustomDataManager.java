package com.houzicore.shared.core.customdata;

import org.bukkit.entity.Player;
import java.util.HashMap;

public class CustomDataManager {
    private static final HashMap<Player, HashMap<String, Object>> _data = new HashMap<>();

    public static void set(Player player, String key, Object value) {
        _data.computeIfAbsent(player, k -> new HashMap<>()).put(key, value);
    }

    public static Object get(Player player, String key) {
        if (!_data.containsKey(player)) return null;
        return _data.get(player).get(key);
    }
    
    public static void clear(Player player) {
        _data.remove(player);
    }
}
