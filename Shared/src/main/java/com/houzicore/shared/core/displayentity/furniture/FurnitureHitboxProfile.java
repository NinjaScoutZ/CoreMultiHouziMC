package com.houzicore.shared.core.displayentity.furniture;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Per-model furniture hitbox override generated under models/hitboxes.
 */
final class FurnitureHitboxProfile {
    private final boolean _interactionEnabled;
    private final float _interactionWidth;
    private final float _interactionHeight;
    private final double _interactionOffsetX;
    private final double _interactionOffsetY;
    private final double _interactionOffsetZ;
    private final boolean _collisionsEnabled;
    private final List<Collision> _collisions;
    private final boolean _liftToGround;
    private final boolean _rotateVisual;
    private final double _yawSnapDegrees;

    private FurnitureHitboxProfile(
            boolean interactionEnabled,
            float interactionWidth,
            float interactionHeight,
            double interactionOffsetX,
            double interactionOffsetY,
            double interactionOffsetZ,
            boolean collisionsEnabled,
            List<Collision> collisions,
            boolean liftToGround,
            boolean rotateVisual,
            double yawSnapDegrees
    ) {
        _interactionEnabled = interactionEnabled;
        _interactionWidth = interactionWidth;
        _interactionHeight = interactionHeight;
        _interactionOffsetX = interactionOffsetX;
        _interactionOffsetY = interactionOffsetY;
        _interactionOffsetZ = interactionOffsetZ;
        _collisionsEnabled = collisionsEnabled;
        _collisions = new ArrayList<>(collisions);
        _liftToGround = liftToGround;
        _rotateVisual = rotateVisual;
        _yawSnapDegrees = yawSnapDegrees;
    }

    static FurnitureHitboxProfile fromFile(File file, FurnitureFootprint fallbackFootprint) {
        if (file == null || !file.isFile()) {
            return defaults(fallbackFootprint);
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        FurnitureHitboxProfile fallback = defaults(fallbackFootprint);
        boolean interactionEnabled = yaml.getBoolean("interaction.enabled", fallback.isInteractionEnabled());
        float interactionWidth = (float) clamp(yaml.getDouble("interaction.width", fallback.getInteractionWidth()), 0.05, 8);
        float interactionHeight = (float) clamp(yaml.getDouble("interaction.height", fallback.getInteractionHeight()), 0.05, 8);
        double interactionOffsetX = yaml.getDouble("interaction.offset.x", fallback.getInteractionOffsetX());
        double interactionOffsetY = yaml.getDouble("interaction.offset.y", fallback.getInteractionOffsetY());
        double interactionOffsetZ = yaml.getDouble("interaction.offset.z", fallback.getInteractionOffsetZ());
        boolean collisionsEnabled = yaml.getBoolean("collisions.enabled", false);
        List<Collision> collisions = readCollisions(yaml, fallback.getCollisions());
        boolean liftToGround = yaml.getBoolean("placement.lift-to-ground", fallback.shouldLiftToGround());
        boolean rotateVisual = yaml.getBoolean("placement.rotate", fallback.shouldRotateVisual());
        double yawSnapDegrees = clamp(yaml.getDouble("placement.yaw-snap-degrees", fallback.getYawSnapDegrees()), 0, 360);

        return new FurnitureHitboxProfile(
                interactionEnabled,
                interactionWidth,
                interactionHeight,
                interactionOffsetX,
                interactionOffsetY,
                interactionOffsetZ,
                collisionsEnabled,
                collisions,
                liftToGround,
                rotateVisual,
                yawSnapDegrees
        );
    }

    static FurnitureHitboxProfile defaults(FurnitureFootprint footprint) {
        FurnitureFootprint effective = footprint != null ? footprint : FurnitureFootprint.defaultFootprint();
        // Interaction entity width is a RADIUS (symmetric cylinder around Y-axis).
        // Use the largest horizontal dimension of the model footprint as the diameter,
        // add a small 2px padding, and clamp to a tighter range so thin furniture stays tight.
        float width = (float) clamp((Math.max(effective.width, effective.depth) / 2.0) + 0.04, 0.20, 2.0);
        float height = (float) clamp(effective.height + 0.04, 0.20, 2.50);
        double collisionSize = clamp(Math.max(effective.width, effective.depth), 0.20, 1.0);
        return new FurnitureHitboxProfile(
                true,
                width,
                height,
                effective.centerX,
                effective.bottomY(),
                effective.centerZ,
                false,
                List.of(new Collision("main", effective.centerX, 0.01, effective.centerZ, collisionSize)),
                true,
                true,
                0
        );
    }

    static void createDefaultFile(File file, String type, String id, FurnitureFootprint footprint) {
        if (file == null || file.exists()) {
            return;
        }

        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        FurnitureHitboxProfile profile = defaults(footprint);
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.options().header(String.join("\n",
                "Auto-generated HouziCore furniture hitbox config.",
                "Edit this file, then run /furniture reload to apply.",
                "Display entities are visual only; Interaction handles clicks and Shulker handles optional collision."
        ));
        yaml.set("type", type);
        yaml.set("id", id);
        yaml.set("placement.lift-to-ground", true);
        yaml.set("placement.rotate", true);
        yaml.set("placement.yaw-snap-degrees", 0);
        yaml.set("interaction.enabled", profile.isInteractionEnabled());
        yaml.set("interaction.width", round(profile.getInteractionWidth()));
        yaml.set("interaction.height", round(profile.getInteractionHeight()));
        yaml.set("interaction.offset.x", round(profile.getInteractionOffsetX()));
        yaml.set("interaction.offset.y", round(profile.getInteractionOffsetY()));
        yaml.set("interaction.offset.z", round(profile.getInteractionOffsetZ()));
        yaml.set("collisions.enabled", false);
        yaml.set("collisions.parts.main.size", round(profile.getCollisions().get(0).size()));
        yaml.set("collisions.parts.main.offset.x", round(profile.getCollisions().get(0).offsetX()));
        yaml.set("collisions.parts.main.offset.y", round(profile.getCollisions().get(0).offsetY()));
        yaml.set("collisions.parts.main.offset.z", round(profile.getCollisions().get(0).offsetZ()));

        try {
            yaml.save(file);
        } catch (IOException ex) {
            System.out.println("[FurnitureHitboxProfile] Failed to create " + file.getAbsolutePath() + ": " + ex.getMessage());
        }
    }

    private static List<Collision> readCollisions(YamlConfiguration yaml, List<Collision> fallback) {
        ConfigurationSection section = yaml.getConfigurationSection("collisions.parts");
        if (section == null) {
            return fallback;
        }

        List<Collision> collisions = new ArrayList<>();
        for (String key : section.getKeys(false)) {
            String path = "collisions.parts." + key;
            double size = clamp(yaml.getDouble(path + ".size", 1.0), 0.05, 4);
            double offsetX = yaml.getDouble(path + ".offset.x", 0);
            double offsetY = yaml.getDouble(path + ".offset.y", 0);
            double offsetZ = yaml.getDouble(path + ".offset.z", 0);
            collisions.add(new Collision(key, offsetX, offsetY, offsetZ, size));
        }

        return collisions.isEmpty() ? fallback : collisions;
    }

    static String safeFileName(String id) {
        String safe = id == null ? "model" : id.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_. -]", "_");
        safe = safe.trim();
        return safe.isEmpty() ? "model" : safe;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    boolean isInteractionEnabled() { return _interactionEnabled; }
    float getInteractionWidth() { return _interactionWidth; }
    float getInteractionHeight() { return _interactionHeight; }
    double getInteractionOffsetX() { return _interactionOffsetX; }
    double getInteractionOffsetY() { return _interactionOffsetY; }
    double getInteractionOffsetZ() { return _interactionOffsetZ; }
    boolean isCollisionsEnabled() { return _collisionsEnabled; }
    List<Collision> getCollisions() { return Collections.unmodifiableList(_collisions); }
    boolean shouldLiftToGround() { return _liftToGround; }
    boolean shouldRotateVisual() { return _rotateVisual; }
    double getYawSnapDegrees() { return _yawSnapDegrees; }

    record Collision(String id, double offsetX, double offsetY, double offsetZ, double size) {
    }
}
