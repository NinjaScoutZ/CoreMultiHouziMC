package com.houzicore.shared.core.displayentity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

/**
 * Central manager for all {@link DisplayModel} instances.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Track all spawned models</li>
 *   <li>Tick animations and entity-following via {@link UpdateEvent}</li>
 *   <li>Cleanup when worlds unload or models are removed</li>
 * </ul>
 * <p>
 * Should be instantiated once per plugin (Hub / ArcadeManager) and passed
 * to any module that needs to spawn Display models.
 * <p>
 * <b>Usage example:</b>
 * <pre>
 * // In Hub.java or ArcadeManager:
 * DisplayEntityManager displayManager = new DisplayEntityManager(this);
 *
 * // Spawn a rotating golden sword above a location:
 * DisplayModel trophy = ModelLoader.rotatingItem("lobby_trophy", Material.GOLDEN_SWORD, 2f);
 * trophy.spawn(someLocation);
 * displayManager.addModel(trophy);
 *
 * // Load a BDEngine model from JSON:
 * DisplayModel chair = ModelLoader.fromFile("lobby_chair", new File("models/chair.json"));
 * chair.spawn(lobbySpawn.clone().add(5, 0, 3));
 * displayManager.addModel(chair);
 *
 * // Cleanup when done:
 * displayManager.removeAll();
 * </pre>
 */
public class DisplayEntityManager implements Listener {

    private final JavaPlugin _plugin;
    private final DisplayModelRegistry _registry;
    private final com.houzicore.shared.core.displayentity.function.BdeFunctionRuntime _functionRuntime;
    private final com.houzicore.shared.core.displayentity.furniture.FurnitureManager _furnitureManager;
    private final List<DisplayModel> _activeModels = new ArrayList<>();

    public DisplayEntityManager(JavaPlugin plugin) {
        _plugin = plugin;
        _registry = new DisplayModelRegistry(plugin);
        _functionRuntime = new com.houzicore.shared.core.displayentity.function.BdeFunctionRuntime(plugin);
        _functionRuntime.reload(_registry.getModelsDirectory());
        com.houzicore.shared.core.displayentity.furniture.FurnitureCatalog.registerAll(_registry);
        _furnitureManager = new com.houzicore.shared.core.displayentity.furniture.FurnitureManager(plugin, this);
        _furnitureManager.ensureHitboxConfigDefaults();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public com.houzicore.shared.core.displayentity.furniture.FurnitureManager getFurnitureManager() {
        return _furnitureManager;
    }

    public com.houzicore.shared.core.displayentity.function.BdeFunctionRuntime getFunctionRuntime() {
        return _functionRuntime;
    }

    /**
     * Get the owning plugin.
     */
    public JavaPlugin getPlugin() {
        return _plugin;
    }

    /**
     * Get the global model registry.
     */
    public DisplayModelRegistry getRegistry() {
        return _registry;
    }

    /**
     * Reload file-backed models while preserving the handcrafted furniture
     * catalog that is registered programmatically at boot.
     */
    public void reloadRegistry() {
        java.io.File modelDir = _registry.getModelsDirectory();
        if (modelDir == null) {
            modelDir = new java.io.File(_plugin.getDataFolder(), "models");
        }
        _registry.loadFromDirectory(modelDir);
        _functionRuntime.reload(modelDir);
        com.houzicore.shared.core.displayentity.furniture.FurnitureCatalog.registerAll(_registry);
        if (_furnitureManager != null) {
            _furnitureManager.ensureHitboxConfigDefaults();
        }
    }

    // ── Model Management ─────────────────────────────

    /**
     * Register a model for tick management.
     * The model should already be spawned.
     */
    public void addModel(DisplayModel model) {
        if (!_activeModels.contains(model)) {
            _activeModels.add(model);
        }
    }

    /**
     * Remove a specific model, destroying its entities.
     */
    public void removeModel(DisplayModel model) {
        model.remove();
        _activeModels.remove(model);
    }

    /**
     * Clean up all models managed by a specific owner lifecycle.
     */
    public void clearOwner(com.houzicore.shared.core.lifecycle.LifecycleOwner owner) {
        if (owner == null) return;
        for (DisplayModel model : new ArrayList<>(_activeModels)) {
            if (model.getOwner() == owner) {
                removeModel(model);
            }
        }
    }

    /**
     * Remove a model by its ID.
     */
    public void removeModel(String modelId) {
        Iterator<DisplayModel> it = _activeModels.iterator();
        while (it.hasNext()) {
            DisplayModel model = it.next();
            if (model.getId().equals(modelId)) {
                model.remove();
                it.remove();
            }
        }
    }

    /**
     * Remove all tracked models and destroy their entities.
     */
    public void removeAll() {
        for (DisplayModel model : new ArrayList<>(_activeModels)) {
            model.remove();
        }
        _activeModels.clear();
    }

    /**
     * Get a model by ID (first match).
     * @return The model, or null if not found
     */
    public DisplayModel getModel(String modelId) {
        for (DisplayModel model : _activeModels) {
            if (model.getId().equals(modelId)) {
                return model;
            }
        }
        return null;
    }

    /**
     * Get all active models.
     */
    public List<DisplayModel> getActiveModels() {
        return new ArrayList<>(_activeModels);
    }

    /**
     * @return Number of active models being tracked
     */
    public int getModelCount() {
        return _activeModels.size();
    }

    // ── Tick ──────────────────────────────────────────

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTick(UpdateEvent event) {
        if (event.getType() != UpdateType.TICK || _activeModels.isEmpty()) {
            return;
        }

        final List<World> worlds = Bukkit.getWorlds();

        Iterator<DisplayModel> it = _activeModels.iterator();
        while (it.hasNext()) {
            DisplayModel model = it.next();

            // Auto-cleanup if world was unloaded
            if (model.isSpawned() && model.getOrigin() != null) {
                if (!worlds.contains(model.getOrigin().getWorld())) {
                    model.remove();
                    it.remove();
                    continue;
                }
            }

            // Tick follow + animation
            if (model.isSpawned()) {
                boolean needsRespawn = false;
                if (model.getOrigin() != null && model.getOrigin().getChunk().isLoaded()) {
                    for (org.bukkit.entity.Display entity : model.getEntities()) {
                        if (entity == null || !entity.isValid()) {
                            needsRespawn = true;
                            break;
                        }
                    }
                }

                if (needsRespawn) {
                    model.spawn(model.getOrigin());
                } else {
                    model.tick();
                }
            }
        }
    }

    // ── Interaction / Seating ────────────────────────

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInteractSeat(org.bukkit.event.player.PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof org.bukkit.entity.Interaction interaction) {
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey("houzicore", "bde_seat");
            if (interaction.getPersistentDataContainer().has(key, org.bukkit.persistence.PersistentDataType.STRING)) {
                // Spawn a temporary ArmorStand to act as a seat
                org.bukkit.entity.ArmorStand seat = (org.bukkit.entity.ArmorStand) interaction.getWorld().spawnEntity(
                        interaction.getLocation().subtract(0, 1.2, 0), // Adjust Y to look natural sitting
                        org.bukkit.entity.EntityType.ARMOR_STAND
                );
                seat.setInvisible(true);
                seat.setMarker(true);
                seat.setGravity(false);
                seat.setInvulnerable(true);
                seat.setPersistent(false);
                
                // Add a tag to know this is a temporary seat
                seat.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.STRING, "active_seat");
                
                seat.addPassenger(event.getPlayer());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDismountSeat(org.bukkit.event.entity.EntityDismountEvent event) {
        if (event.getDismounted() instanceof org.bukkit.entity.ArmorStand seat) {
            org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey("houzicore", "bde_seat");
            if (seat.getPersistentDataContainer().has(key, org.bukkit.persistence.PersistentDataType.STRING)) {
                // Player stood up, remove the temporary seat
                seat.remove();
            }
        }
    }

    // ── Convenience Spawn Methods ────────────────────

    /**
     * Spawn a model at a location and register it in one call.
     */
    public DisplayModel spawnModel(DisplayModel model, org.bukkit.Location location) {
        model.spawn(location);
        addModel(model);
        return model;
    }

    /**
     * Arcade Drop Visualizer (Toy #3)
     * Spawns a floating, scaled-down chest block above the location that bobs up and down.
     * Useful for Arcade games to highlight loot drops (like Care Packages).
     */
    public DisplayModel spawnLootVisualizer(org.bukkit.Location location) {
        DisplayPart chestPart = DisplayPart.block(org.bukkit.Material.CHEST);
        chestPart.translation(new org.joml.Vector3f(-0.3f, 0f, -0.3f));
        chestPart.scale(new org.joml.Vector3f(0.6f, 0.6f, 0.6f));

        DisplayModel lootModel = new DisplayModel("loot_visualizer_" + System.currentTimeMillis(), chestPart);
        lootModel.setAnimation(ModelAnimation.bob(0.4f));

        // Spawn slightly above the exact ground location
        lootModel.spawn(location.clone().add(0, 1.2, 0));
        addModel(lootModel);

        return lootModel;
    }
}
