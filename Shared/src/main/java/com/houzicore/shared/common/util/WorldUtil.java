package com.houzicore.shared.common.util;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

public class WorldUtil {
    public static World LoadWorld(WorldCreator creator) {
        return Bukkit.createWorld(creator);
    }
}
