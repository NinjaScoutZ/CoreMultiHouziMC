package com.houzicore.mapparser;

import org.bukkit.Location;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class SchemaExporter {

    public static void export(File directory, MapData mapData, Location cornerA, Location cornerB, 
                              HashMap<String, ArrayList<Location>> teamLocs, 
                              HashMap<String, ArrayList<Location>> dataLocs, 
                              HashMap<String, ArrayList<Location>> customLocs) {
        
        JsonObject schema = new JsonObject();
        schema.addProperty("schemaVersion", 1);
        schema.addProperty("format", "HOUZICORE_HYBRID_MAP");
        schema.addProperty("gameType", mapData.MapGameType.name());
        schema.addProperty("mapName", mapData.MapName);
        schema.addProperty("author", mapData.MapCreator);

        // Bounds
        JsonObject bounds = new JsonObject();
        bounds.addProperty("minX", Math.min(cornerA.getBlockX(), cornerB.getBlockX()));
        bounds.addProperty("maxX", Math.max(cornerA.getBlockX(), cornerB.getBlockX()));
        bounds.addProperty("minY", Math.min(cornerA.getBlockY(), cornerB.getBlockY()));
        bounds.addProperty("maxY", Math.max(cornerA.getBlockY(), cornerB.getBlockY()));
        bounds.addProperty("minZ", Math.min(cornerA.getBlockZ(), cornerB.getBlockZ()));
        bounds.addProperty("maxZ", Math.max(cornerA.getBlockZ(), cornerB.getBlockZ()));
        schema.add("bounds", bounds);

        JsonObject dataPoints = new JsonObject();

        // Team Spawns
        for (String team : teamLocs.keySet()) {
            dataPoints.add("TEAM_NAME:" + team, createLocArray(teamLocs.get(team)));
        }

        // Data Locs
        for (String data : dataLocs.keySet()) {
            dataPoints.add("DATA_NAME:" + data, createLocArray(dataLocs.get(data)));
        }

        // Custom Locs
        for (String custom : customLocs.keySet()) {
            dataPoints.add("CUSTOM_NAME:" + custom, createLocArray(customLocs.get(custom)));
        }

        schema.add("dataPoints", dataPoints);
        
        schema.add("properties", new JsonObject()); // empty properties for parity

        try (FileWriter writer = new FileWriter(new File(directory, "schema.json"))) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(schema, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JsonArray createLocArray(ArrayList<Location> locs) {
        JsonArray array = new JsonArray();
        for (Location loc : locs) {
            JsonObject obj = new JsonObject();
            obj.addProperty("x", loc.getBlockX());
            obj.addProperty("y", loc.getBlockY());
            obj.addProperty("z", loc.getBlockZ());
            array.add(obj);
        }
        return array;
    }
}
