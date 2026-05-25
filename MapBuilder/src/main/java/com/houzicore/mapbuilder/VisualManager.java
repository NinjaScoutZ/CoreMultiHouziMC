package com.houzicore.mapbuilder;

import com.houzicore.mapbuilder.domain.MapPointDefinition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class VisualManager implements Listener {

    private static final String MAPBUILDER_VISUAL_TAG = "houzicore_mapbuilder_visual";

    private static VisualManager instance;
    private final Map<Location, ArmorStand> activeHolograms = new HashMap<>();
    private final Map<Location, String> activeParticles = new HashMap<>();
    private final Map<Location, com.houzicore.shared.core.displayentity.DisplayModel> activeDisplayModels = new HashMap<>();
    private final Map<Location, Display> activeBlockDisplays = new HashMap<>();
    private final Map<Location, UUID> visualOwners = new HashMap<>();
    private final Map<UUID, Set<Location>> ownerVisuals = new HashMap<>();

    public VisualManager() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, MapBuilderPlugin.getInstance());
    }

    public static VisualManager getInstance() {
        return instance;
    }

    public void spawnVisual(String type, Location loc) {
        spawnVisual(null, type, loc);
    }

    public void spawnVisual(UUID ownerId, String type, Location loc) {
        Location key = toVisualKey(loc);
        removeVisual(loc);

        // ── Block Display entity (single block) ────────────────
        if (type.startsWith("BLOCK_DISPLAY:")) {
            String matName = type.substring("BLOCK_DISPLAY:".length());
            try {
                Material mat = Material.valueOf(matName);
                Location spawnLoc = loc.clone().add(0.5, 0, 0.5); // Center on block
                BlockDisplay bd = (BlockDisplay) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.BLOCK_DISPLAY);
                markTemporary(bd);
                bd.setBlock(mat.createBlockData());
                bd.setGlowing(true);
                activeBlockDisplays.put(key, bd);
                activeParticles.put(key, type);
                trackOwner(ownerId, key);
            } catch (IllegalArgumentException e) {
                // Invalid material — fall through to default hologram
            }
            return;
        }

        // ── Display Model (multi-part JSON model) ─────────────
        if (type.startsWith("DISPLAY_MODEL:")) {
            String encodedData = type.substring("DISPLAY_MODEL:".length());
            String[] parts = encodedData.split(":");
            String modelId = parts[0];
            
            com.houzicore.shared.core.displayentity.DisplayModel blueprint = MapBuilderPlugin.getInstance().getDisplayEntityManager().getRegistry().getModel(modelId);
            if (blueprint != null) {
                com.houzicore.shared.core.displayentity.DisplayModel clone = blueprint.copy(modelId + "_" + loc.hashCode());
                
                // Decode transformations if present [yaw,pitch,sx,sy,sz,ox,oy,oz]
                if (parts.length > 1) {
                    try {
                        String[] tf = parts[1].split(",");
                        if (tf.length >= 8) {
                            float yaw = Float.parseFloat(tf[0]);
                            float pitch = Float.parseFloat(tf[1]);
                            float sx = Float.parseFloat(tf[2]);
                            float sy = Float.parseFloat(tf[3]);
                            float sz = Float.parseFloat(tf[4]);
                            float ox = Float.parseFloat(tf[5]);
                            float oy = Float.parseFloat(tf[6]);
                            float oz = Float.parseFloat(tf[7]);
                            
                            // Apply rotation to origin
                            Location modifiedOrigin = loc.clone();
                            modifiedOrigin.setYaw(yaw);
                            modifiedOrigin.setPitch(pitch);
                            modifiedOrigin.add(ox, oy, oz);
                            loc = modifiedOrigin; // Let the DisplayModel spawn here natively

                            // Scale logic: We'll apply this to parts during model editor creation later
                        }
                    } catch (Exception e) {}
                }

                clone.spawn(loc);
                clone.getEntities().forEach(this::markTemporary);
                clone.getAuxiliaryEntities().forEach(this::markTemporary);
                MapBuilderPlugin.getInstance().getDisplayEntityManager().addModel(clone);
                activeDisplayModels.put(key, clone);
                activeParticles.put(key, type);
                trackOwner(ownerId, key);
                return; // Suppress the ugly armorstand
            }
        }

        // Spawn Hologram
        ArmorStand stand = (ArmorStand) loc.getWorld().spawnEntity(loc.clone().add(0, 0.5, 0), EntityType.ARMOR_STAND);
        markTemporary(stand);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setMarker(true);
        stand.setCustomNameVisible(true);
        stand.setCustomName(getDisplayNameForType(type));
        
        activeHolograms.put(key, stand);
        activeParticles.put(key, type);
        trackOwner(ownerId, key);
    }

    public void removeVisual(Location loc) {
        Location key = toVisualKey(loc);

        if (activeHolograms.containsKey(key)) {
            activeHolograms.remove(key).remove();
        }
        if (activeDisplayModels.containsKey(key)) {
            com.houzicore.shared.core.displayentity.DisplayModel model = activeDisplayModels.remove(key);
            MapBuilderPlugin.getInstance().getDisplayEntityManager().removeModel(model);
        }
        if (activeBlockDisplays.containsKey(key)) {
            Display bd = activeBlockDisplays.remove(key);
            if (bd != null && bd.isValid()) bd.remove();
        }
        activeParticles.remove(key);
        untrackOwner(key);
    }

    public void clearVisuals(UUID ownerId) {
        if (ownerId == null) {
            return;
        }

        Set<Location> locations = ownerVisuals.get(ownerId);
        if (locations == null || locations.isEmpty()) {
            ownerVisuals.remove(ownerId);
            return;
        }

        for (Location loc : new HashSet<>(locations)) {
            removeVisual(loc);
        }

        ownerVisuals.remove(ownerId);
    }

    public void clearAllVisuals() {
        for (ArmorStand stand : activeHolograms.values()) {
            stand.remove();
        }
        for (com.houzicore.shared.core.displayentity.DisplayModel model : activeDisplayModels.values()) {
            MapBuilderPlugin.getInstance().getDisplayEntityManager().removeModel(model);
        }
        for (Display bd : activeBlockDisplays.values()) {
            if (bd != null && bd.isValid()) bd.remove();
        }
        activeHolograms.clear();
        activeDisplayModels.clear();
        activeBlockDisplays.clear();
        activeParticles.clear();
        visualOwners.clear();
        ownerVisuals.clear();
    }

    public void updateBoundaryVisual(MapSession session) {
        // Here we could draw lines using particles between min and max
        // To be simple, we just leave it to the map builder to conceptually know where it is, 
        // or we can add complex particle box drawing.
    }

    private String getDisplayNameForType(String type) {
        // First try to resolve via the canonical MapPointDefinition catalog
        MapPointDefinition def = MapPointDefinition.fromExportKey(type);
        if (def != null) return def.category.displayName.substring(0, 2) + def.displayName;

        // Fallback for non-catalog types (BLOCK_DISPLAY, DISPLAY_MODEL, etc.)
        if (type.startsWith("BLOCK_DISPLAY:")) return ChatColor.GOLD + "Block: " + type.substring("BLOCK_DISPLAY:".length());
        if (type.startsWith("DISPLAY_MODEL:")) return ChatColor.YELLOW + "Model: " + type.substring("DISPLAY_MODEL:".length());
        return ChatColor.WHITE + type;
    }

    @EventHandler
    public void onUpdate(com.houzicore.shared.updater.event.UpdateEvent event) {
        if (event.getType() != com.houzicore.shared.updater.UpdateType.FAST) {
            return;
        }

        // 1. Existing Points
        for (Map.Entry<Location, String> entry : activeParticles.entrySet()) {
            Location loc = entry.getKey().clone().add(0.5, 0.5, 0.5);
            String type = entry.getValue();

            if (type.equals("DATA_NAME:WATERFALL_EMITTER")) {
                loc.getWorld().spawnParticle(Particle.DRIPPING_WATER, loc, 4, 0.3, 0.1, 0.3, 0);
            } else if (type.equals("TEAM_NAME:Blue")) {
                loc.getWorld().spawnParticle(Particle.DUST, loc, 3, 0.1, 0.1, 0.1, new Particle.DustOptions(Color.AQUA, 1));
            } else if (type.equals("TEAM_NAME:Red")) {
                loc.getWorld().spawnParticle(Particle.DUST, loc, 3, 0.1, 0.1, 0.1, new Particle.DustOptions(Color.RED, 1));
            } else if (type.equals("DATA_NAME:BLACK")) {
                loc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc, 1, 0, 0, 0, 0);
            } else if (type.startsWith("BLOCK_DISPLAY:")) {
                loc.getWorld().spawnParticle(Particle.DUST, loc.clone().add(0, 1, 0), 3, 0.2, 0.2, 0.2, new Particle.DustOptions(Color.ORANGE, 0.8f));
            } else if (type.startsWith("DATA_NAME:")) {
                loc.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 2, 0.2, 0.2, 0.2, 0);
            } else {
                loc.getWorld().spawnParticle(Particle.CLOUD, loc, 1, 0, 0, 0, 0);
            }
        }

        // 2. Bound Visualization (WorldEditSUI style box)
        for (MapSession s : MapBuilderPlugin.getInstance().getActiveSessions()) {
            if (s.getMinBoundary() != null && s.getMaxBoundary() != null && s.getBuilder().isOnline()) {
                Location min = s.getMinBoundary();
                Location max = s.getMaxBoundary();
                org.bukkit.World w = min.getWorld();
                int minX = Math.min(min.getBlockX(), max.getBlockX());
                int minZ = Math.min(min.getBlockZ(), max.getBlockZ());
                int maxX = Math.max(min.getBlockX(), max.getBlockX()) + 1; // +1 to trace outside edge
                int maxZ = Math.max(min.getBlockZ(), max.getBlockZ()) + 1;

                // To not overwhelm the client, we draw near the Builder's Y level
                int py = s.getBuilder().getLocation().getBlockY();
                int drawMinY = Math.max(-64, py - 20);
                int drawMaxY = Math.min(320, py + 20);

                int step = 2; // Particle density

                // Horizontal box at the player's Y level
                for (int x = minX; x <= maxX; x += step) {
                    w.spawnParticle(Particle.FLAME, new Location(w, x, py, minZ), 1, 0, 0, 0, 0);
                    w.spawnParticle(Particle.FLAME, new Location(w, x, py, maxZ), 1, 0, 0, 0, 0);
                }
                for (int z = minZ; z <= maxZ; z += step) {
                    w.spawnParticle(Particle.FLAME, new Location(w, minX, py, z), 1, 0, 0, 0, 0);
                    w.spawnParticle(Particle.FLAME, new Location(w, maxX, py, z), 1, 0, 0, 0, 0);
                }

                // Four Vertical pillars tracking the Y-level bounds
                for (int y = drawMinY; y <= drawMaxY; y += step) {
                    w.spawnParticle(Particle.FLAME, new Location(w, minX, y, minZ), 1, 0, 0, 0, 0);
                    w.spawnParticle(Particle.FLAME, new Location(w, maxX, y, minZ), 1, 0, 0, 0, 0);
                    w.spawnParticle(Particle.FLAME, new Location(w, minX, y, maxZ), 1, 0, 0, 0, 0);
                    w.spawnParticle(Particle.FLAME, new Location(w, maxX, y, maxZ), 1, 0, 0, 0, 0);
                }
            }
        }
    }

    
    public void cleanup() {
        clearAllVisuals();
    }

    private void markTemporary(Entity entity) {
        entity.setPersistent(false);
        entity.addScoreboardTag(MAPBUILDER_VISUAL_TAG);
    }

    private void trackOwner(UUID ownerId, Location key) {
        if (ownerId == null) {
            return;
        }

        visualOwners.put(key, ownerId);
        ownerVisuals.computeIfAbsent(ownerId, ignored -> new HashSet<>()).add(key);
    }

    private void untrackOwner(Location key) {
        UUID ownerId = visualOwners.remove(key);
        if (ownerId == null) {
            return;
        }

        Set<Location> locations = ownerVisuals.get(ownerId);
        if (locations == null) {
            return;
        }

        locations.remove(key);
        if (locations.isEmpty()) {
            ownerVisuals.remove(ownerId);
        }
    }

    private Location toVisualKey(Location loc) {
        return new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
}
