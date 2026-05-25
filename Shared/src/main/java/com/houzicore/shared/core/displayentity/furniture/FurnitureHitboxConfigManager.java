package com.houzicore.shared.core.displayentity.furniture;

import java.io.File;

import com.houzicore.shared.core.displayentity.DisplayModel;
import com.houzicore.shared.core.displayentity.DisplayModelRegistry;
import com.houzicore.shared.core.displayentity.function.BdeFunctionPack;
import com.houzicore.shared.core.displayentity.function.BdeFunctionRuntime;

/**
 * Creates and reads per-model hitbox configs for placeable display furniture.
 */
final class FurnitureHitboxConfigManager {
    private final File _hitboxDirectory;

    FurnitureHitboxConfigManager(File modelsDirectory) {
        File base = modelsDirectory != null ? modelsDirectory : new File("models");
        _hitboxDirectory = new File(base, "hitboxes");
    }

    void ensureDefaults(DisplayModelRegistry registry, BdeFunctionRuntime functionRuntime) {
        if (registry != null) {
            for (String modelId : registry.getFileBackedModelIds()) {
                DisplayModel model = registry.getModel(modelId);
                if (model == null) {
                    continue;
                }
                FurnitureHitboxProfile.createDefaultFile(
                        staticFile(modelId),
                        "static",
                        modelId,
                        FurnitureFootprint.fromModel(model)
                );
            }
        }

        if (functionRuntime != null) {
            for (BdeFunctionPack pack : functionRuntime.getPacks()) {
                String namespace = pack.getNamespace();
                FurnitureHitboxProfile.createDefaultFile(
                        functionFile(namespace),
                        "function",
                        namespace,
                        FurnitureFootprint.fromFunction(functionRuntime.estimateFootprint(namespace))
                );
            }
        }
    }

    FurnitureHitboxProfile loadStatic(String modelId, FurnitureFootprint fallbackFootprint) {
        return FurnitureHitboxProfile.fromFile(staticFile(modelId), fallbackFootprint);
    }

    /**
     * Returns true if a hitbox config YAML file exists on disk for this model.
     * Used to distinguish "user has explicitly configured hitbox" from "use auto-scan".
     */
    boolean hasCustomFile(String modelId) {
        return staticFile(modelId).isFile();
    }

    boolean hasCustomFunctionFile(String namespace) {
        return functionFile(namespace).isFile();
    }

    FurnitureHitboxProfile loadFunction(String namespace, FurnitureFootprint fallbackFootprint) {
        return FurnitureHitboxProfile.fromFile(functionFile(namespace), fallbackFootprint);
    }

    private File staticFile(String modelId) {
        return new File(new File(_hitboxDirectory, "static"), FurnitureHitboxProfile.safeFileName(modelId) + ".yml");
    }

    private File functionFile(String namespace) {
        return new File(new File(_hitboxDirectory, "function"), FurnitureHitboxProfile.safeFileName(namespace) + ".yml");
    }
}
