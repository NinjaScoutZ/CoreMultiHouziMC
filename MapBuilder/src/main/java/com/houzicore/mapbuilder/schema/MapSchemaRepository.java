package com.houzicore.mapbuilder.schema;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.logging.Logger;

/**
 * Handles the persistence of MapSchema into a canonical JSON file.
 * This is the MB-05 implementation of Schema Persistence.
 */
public class MapSchemaRepository {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger logger = Logger.getLogger("MapBuilder-SchemaRepo");

    /**
     * Loads the schema.json from the specified directory.
     * @param directory The world folder (e.g., Lobby or temporary edit world)
     * @return The MapSchema if valid, null otherwise.
     */
    public static MapSchema load(File directory) {
        File file = new File(directory, "schema.json");
        if (!file.exists()) {
            return null;
        }

        try (Reader reader = new FileReader(file)) {
            return gson.fromJson(reader, MapSchema.class);
        } catch (IOException | JsonSyntaxException e) {
            logger.severe("Failed to load schema.json from " + directory.getPath() + ": " + e.getMessage());
            return null;
        }
    }

    public static boolean save(File directory, MapSchema schema) {
        return save(directory, "schema.json", schema);
    }

    /**
     * Saves the MapSchema out to the specified file in the directory.
     */
    public static boolean save(File directory, String fileName, MapSchema schema) {
        File file = new File(directory, fileName);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (Writer writer = new FileWriter(file)) {
            gson.toJson(schema, writer);
            return true;
        } catch (IOException e) {
            logger.severe("Failed to save " + fileName + " to " + directory.getPath() + ": " + e.getMessage());
            return false;
        }
    }
}
