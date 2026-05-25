package com.houzicore.mapbuilder;

import com.houzicore.mapbuilder.session.BuilderSessionState;
import com.houzicore.mapbuilder.template.MapTemplate;
import com.houzicore.mapbuilder.template.MapTemplateRegistry;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapSession {

    private final Player builder;
    private final String gameType;
    private final String mapName;
    private final String author;
    private final String editWorldName;
    private final World editWorld;
    private final File worldConfigFile;
    private final com.houzicore.mapbuilder.session.EditMode mode;

    private Location minBoundary;
    private Location maxBoundary;

    // Raw map data — never changes format (backward-compat with WorldConfigExporter)
    private final Map<String, List<Location>> dataPoints = new HashMap<>();

    // MB-09D: per-session arbitrary properties (e.g. DISGUISE_TYPE=SKELETON)
    private final Map<String, String> properties = new HashMap<>();

    // New: per-session state (selection, undo/redo, template)
    private final BuilderSessionState state;

    public MapSession(Player builder, String gameType, String mapName, String author, com.houzicore.mapbuilder.session.EditMode mode) {
        this(builder, gameType, mapName, author, builder.getWorld(), mode);
    }

    public MapSession(Player builder, String gameType, String mapName, String author, World editWorld, com.houzicore.mapbuilder.session.EditMode mode) {
        this.builder   = builder;
        this.gameType  = gameType;
        this.mapName   = mapName;
        this.author    = author;
        this.mode      = mode;
        this.editWorld     = editWorld;
        this.editWorldName = editWorld.getName();
        if (mode == com.houzicore.mapbuilder.session.EditMode.SANDBOX) {
            File pluginDir = com.houzicore.mapbuilder.MapBuilderPlugin.getInstance().getDataFolder();
            this.worldConfigFile = new File(pluginDir, "sandbox_exports" + File.separator + mapName + File.separator + "WorldConfig.dat");
        } else {
            this.worldConfigFile = new File(editWorld.getWorldFolder(), "WorldConfig.dat");
        }
        MapTemplate template = MapTemplateRegistry.getInstance().get(gameType);
        this.state = new BuilderSessionState(template);
    }

    // ── Data points ──────────────────────────────────────────────────────────

    public void addDataPoint(String type, Location loc) {
        dataPoints.computeIfAbsent(type, k -> new ArrayList<>()).add(loc);
        // Recording to undo stack is done by the tool handlers via getState()
    }

    public void removeDataPoint(String type, Location loc) {
        List<Location> list = dataPoints.get(type);
        if (list != null) list.remove(loc);
    }

    /** Remove a point by closest match within tolerance (used by EraserTool). */
    public boolean removeNearestDataPoint(Location target, double radiusSq) {
        String bestType = null;
        Location bestLoc = null;
        double bestDist = radiusSq;

        for (Map.Entry<String, List<Location>> entry : dataPoints.entrySet()) {
            for (Location loc : entry.getValue()) {
                if (!loc.getWorld().equals(target.getWorld())) continue;
                double d = loc.distanceSquared(target);
                if (d < bestDist) {
                    bestDist = d;
                    bestType = entry.getKey();
                    bestLoc  = loc;
                }
            }
        }

        if (bestType != null) {
            dataPoints.get(bestType).remove(bestLoc);
            return true;
        }
        return false;
    }

    // ── Counts ───────────────────────────────────────────────────────────────

    public int countPoints(String exportKey) {
        List<Location> list = dataPoints.get(exportKey);
        return list == null ? 0 : list.size();
    }

    // ── Properties ──────────────────────────────────────────────────────────

    /**
     * Returns the current session properties (unmodifiable view for reads,
     * mutate via setProperty / deleteProperty).
     */
    public Map<String, String> getProperties() { return properties; }

    public String getProperty(String key) { return properties.get(key); }

    /**
     * Sets a custom property. Reserved dat keys are rejected silently here;
     * callers (command handlers) should validate before calling.
     */
    public void setProperty(String key, String value) {
        properties.put(key, value);
    }

    public boolean deleteProperty(String key) {
        return properties.remove(key) != null;
    }

    /** Bulk-load properties (used by WorldImporter when restoring from disk). */
    public void loadProperties(Map<String, String> source) {
        properties.clear();
        if (source != null) properties.putAll(source);
    }

    // ── Boundaries ────────────────────────────────────────────────────────────

    public Location getMinBoundary()               { return minBoundary; }
    public void     setMinBoundary(Location loc)   { this.minBoundary = loc; }
    public Location getMaxBoundary()               { return maxBoundary; }
    public void     setMaxBoundary(Location loc)   { this.maxBoundary = loc; }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public Player                    getBuilder()    { return builder; }
    public String                    getGameType()   { return gameType; }
    public String                    getMapName()    { return mapName; }
    public String                    getAuthor()     { return author; }
    public World                     getEditWorld()  { return editWorld; }
    public String                    getEditWorldName() { return editWorldName; }
    public File                      getWorldConfigFile() { return worldConfigFile; }
    public com.houzicore.mapbuilder.session.EditMode getMode() { return mode; }
    public Map<String, List<Location>> getDataPoints(){ return dataPoints; }
    public BuilderSessionState       getState()      { return state; }

    public void clearSession() {
        dataPoints.clear();
        properties.clear();
        state.clear();
        minBoundary = null;
        maxBoundary = null;
    }
}
