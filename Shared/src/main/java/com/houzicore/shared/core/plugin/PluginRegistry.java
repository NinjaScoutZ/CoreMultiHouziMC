package com.houzicore.shared.core.plugin;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.plugin.java.JavaPlugin;
import com.houzicore.shared.MiniPlugin;

public class PluginRegistry {

    private static final Map<Class<? extends MiniPlugin>, MiniPlugin> _plugins = new HashMap<>();
    private static JavaPlugin _hostPlugin;

    public static void initialize(JavaPlugin hostPlugin) {
        _hostPlugin = hostPlugin;
    }

    public static void register(MiniPlugin plugin) {
        _plugins.put(plugin.getClass(), plugin);
    }

    @SuppressWarnings("unchecked")
    public static <T extends MiniPlugin> T require(Class<T> clazz) {
        if (_plugins.containsKey(clazz)) {
            return (T) _plugins.get(clazz);
        }

        if (clazz.isAnnotationPresent(ReflectivelyCreateMiniPlugin.class)) {
            if (_hostPlugin == null) {
                throw new IllegalStateException("PluginRegistry has not been initialized with a host JavaPlugin.");
            }

            try {
                Constructor<T> constructor = clazz.getConstructor(JavaPlugin.class);
                T instance = constructor.newInstance(_hostPlugin);
                _plugins.put(clazz, instance);
                return instance;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to reflectively create MiniPlugin: " + clazz.getSimpleName(), e);
            }
        }

        throw new IllegalArgumentException("MiniPlugin " + clazz.getSimpleName() + " is not registered and does not have @ReflectivelyCreateMiniPlugin.");
    }
}
