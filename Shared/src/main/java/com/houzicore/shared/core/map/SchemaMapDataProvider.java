package com.houzicore.shared.core.map;

import com.houzicore.shared.api.map.MapDataProvider;
import com.houzicore.shared.api.map.MapDefinition;

import java.io.File;
import java.util.Optional;

/**
 * A MapDataProvider that reads schema.json (written by MapBuilder) and wraps
 * it as a MapDefinition via SchemaMapDefinition.
 *
 * This is the "modern" Shared-side provider. It does NOT know about MapBuilder
 * internals — it only understands the schema.json file format, which is owned
 * by the shared contract layer.
 *
 * Lookup convention:
 *   load(mapName, gameType) → looks in Maps/<gameType>/<mapName>/schema.json
 *   loadFromDirectory(dir)  → looks for schema.json directly in <dir>
 */
public class SchemaMapDataProvider implements MapDataProvider {

    @Override
    public Optional<MapDefinition> load(String mapName, String gameType) {
        File dir = new File("Maps/" + gameType + "/" + mapName);
        if (!dir.exists() || !dir.isDirectory()) return Optional.empty();
        return loadFromDirectory(dir);
    }

    @Override
    public Optional<MapDefinition> loadFromDirectory(File directory) {
        SchemaMapDefinition def = SchemaMapDefinition.loadFrom(directory);
        return Optional.ofNullable(def);
    }

    @Override
    public boolean exists(String mapName, String gameType) {
        File schema = new File("Maps/" + gameType + "/" + mapName + "/schema.json");
        return schema.exists();
    }
}
