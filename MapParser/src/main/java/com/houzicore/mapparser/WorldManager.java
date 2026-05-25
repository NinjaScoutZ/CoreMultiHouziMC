package com.houzicore.mapparser;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.Bukkit;

import java.io.File;
import com.houzicore.shared.common.util.FileUtil;
import com.houzicore.shared.common.util.MapUtil;

public class WorldManager {

    private final MapParserPlugin plugin;

    public WorldManager(MapParserPlugin plugin) {
        this.plugin = plugin;
    }

    public World prepMapParse(World world) {
        String worldName = world.getName();
        File sourceFolder = new File(worldName);
        
        // Save world before unloading so recent marker placements aren't lost!
        world.save();
        MapUtil.UnloadWorld(plugin, world, true);

        // We could just load it again, but usually it copies to a temp folder if needed.
        // For simplicity, we just load it back up so Parse can access it.
        // In 1.21.11, MapUtil handles saving and unloading.
        World parsedWorld = Bukkit.createWorld(new WorldCreator(worldName));
        return parsedWorld;
    }
}
