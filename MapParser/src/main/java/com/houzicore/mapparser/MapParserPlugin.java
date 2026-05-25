package com.houzicore.mapparser;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.mapparser.command.ParseCommand;
import com.houzicore.mapparser.command.MapCommand;
import com.houzicore.mapparser.listener.MarkerFeedbackListener;
import com.houzicore.mapparser.scoreboard.MapParserScoreboard;
import com.houzicore.mapparser.tools.MapToolManager;

import java.util.HashMap;
import java.util.Map;

public class MapParserPlugin extends JavaPlugin {
    
    private static MapParserPlugin instance;
    private WorldManager worldManager;
    private Parse currentParse = null;
    private MapParserScoreboard scoreboard;
    private MapToolManager toolManager;
    private com.houzicore.shared.core.scoreboard.ScoreboardManager localScoreboardManager;
    
    private final Map<String, MapData> mapDataMap = new HashMap<>();
    private Location spawnLocation;

    @Override
    public void onEnable() {
        instance = this;
        worldManager = new WorldManager(this);

        // Set main spawn location
        World firstWorld = getServer().getWorlds().get(0);
        firstWorld.setSpawnLocation(0, 106, 0);
        spawnLocation = new Location(firstWorld, 0, 106, 0);

        // Register Commands with Tab Completers
        ParseCommand parseCmd = new ParseCommand(this);
        getCommand("parse").setExecutor(parseCmd);
        getCommand("parse").setTabCompleter(parseCmd);

        MapCommand mapCmd = new MapCommand(this);
        getCommand("map").setExecutor(mapCmd);
        getCommand("map").setTabCompleter(mapCmd);

        // Register Listeners
        getServer().getPluginManager().registerEvents(new MarkerFeedbackListener(), this);
        toolManager = new MapToolManager(this);
        getServer().getPluginManager().registerEvents(toolManager, this);

        // Start Scoreboard
        if (com.houzicore.shared.core.scoreboard.ScoreboardManager.getInstance() == null) {
            localScoreboardManager = new com.houzicore.shared.core.scoreboard.ScoreboardManager(this, null, null);
        }
        scoreboard = new MapParserScoreboard(this);
        getServer().getPluginManager().registerEvents(scoreboard, this);

        getLogger().info("MapParser (1.21.11) Enabled — /parse check for dry-run, /map for setup");
    }

    @Override
    public void onDisable() {
        if (scoreboard != null) scoreboard.cleanup();
        if (localScoreboardManager != null) {
            localScoreboardManager.onDisable();
        }
        getLogger().info("MapParser Disabled!");
    }

    public static MapParserPlugin getInstance() {
        return instance;
    }

    public WorldManager getWorldManager() {
        return worldManager;
    }

    public Parse getCurrentParse() {
        return currentParse;
    }

    public void setCurrentParse(Parse parse) {
        this.currentParse = parse;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public MapToolManager getToolManager() {
        return toolManager;
    }

    public MapData getData(String mapName) {
        if (mapDataMap.containsKey(mapName)) {
            return mapDataMap.get(mapName);
        }
        MapData data = new MapData(mapName);
        mapDataMap.put(mapName, data);
        return data;
    }

    public void announce(String msg) {
        getServer().broadcastMessage("§6" + msg);
        getLogger().info("[Announce] " + msg);
    }
}
