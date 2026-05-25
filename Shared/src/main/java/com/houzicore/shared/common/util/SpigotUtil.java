package com.houzicore.shared.common.util;

import org.bukkit.Bukkit;

public class SpigotUtil {
    public static boolean isVersion(int major, int minor, int patch) {
        String version = Bukkit.getServer().getVersion(); 
        return version.contains(major + "." + minor);
    }
}
