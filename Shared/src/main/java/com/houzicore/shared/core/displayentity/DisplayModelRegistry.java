package com.houzicore.shared.core.displayentity;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Registry for loading and globally storing DisplayModel prototypes.
 * BDEngine JSON exported files can be dropped into plugins/HouziCore/models/
 * to be automatically loaded upon boot and used by any minigame or Lobby.
 */
public class DisplayModelRegistry {

	private final Map<String, DisplayModel> _models = new HashMap<>();
	private final Set<String> _fileBackedModelIds = new HashSet<>();
	private File _modelsDirectory;

	public DisplayModelRegistry(JavaPlugin plugin) {
		loadFromDirectory(new File(plugin.getDataFolder(), "models"));
	}

	/**
	 * Loads all JSON model definitions from the designated directory.
	 */
	public void loadFromDirectory(File dir) {
		_modelsDirectory = dir;
		_models.clear();
		_fileBackedModelIds.clear();
		if (!dir.exists()) {
			dir.mkdirs();
			return;
		}

		File[] files = dir.listFiles((d, name) -> name.endsWith(".json") || name.endsWith(".bdstudio") || name.endsWith(".bdengine"));
		if (files == null) return;

		for (File file : files) {
			String fileName = file.getName();
			String modelId = fileName.substring(0, fileName.lastIndexOf('.')).toLowerCase();
			try {
				DisplayModel model = ModelLoader.fromFile(modelId, file);
				_models.put(modelId, model);
				_fileBackedModelIds.add(modelId);
				System.out.println("[DisplayModelRegistry] Successfully loaded model: " + modelId);
			} catch (Exception e) {
				System.out.println("[DisplayModelRegistry] Failed to load model file " + file.getName() + " - " + e.getMessage());
			}
		}
	}

	/**
	 * Registers a programmatically created blueprint.
	 */
	public void registerModel(DisplayModel model) {
		_models.put(model.getId(), model);
	}

	/**
	 * Retrieves a blueprint by its ID for cloning/spawning.
	 */
	public DisplayModel getModel(String id) {
		return _models.get(id);
	}

	/**
	 * Returns all loaded model blueprints.
	 */
	public Collection<DisplayModel> getModels() {
		return _models.values();
	}

	public Set<String> getFileBackedModelIds() {
		return Collections.unmodifiableSet(_fileBackedModelIds);
	}

	public File getModelsDirectory() {
		return _modelsDirectory;
	}
}
