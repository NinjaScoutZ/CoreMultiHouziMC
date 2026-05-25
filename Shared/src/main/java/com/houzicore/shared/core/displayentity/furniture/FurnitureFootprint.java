package com.houzicore.shared.core.displayentity.furniture;

import com.houzicore.shared.core.displayentity.DisplayModel;
import com.houzicore.shared.core.displayentity.DisplayPart;
import com.houzicore.shared.core.displayentity.function.BdeFunctionRuntime.FunctionFootprint;

/**
 * Compact local-space footprint used to seed furniture hitbox configs.
 */
final class FurnitureFootprint {
    final double centerX;
    final double centerY;
    final double centerZ;
    final double width;
    final double height;
    final double depth;

    private FurnitureFootprint(double centerX, double centerY, double centerZ, double width, double height, double depth) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    static FurnitureFootprint fromModel(DisplayModel model) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        for (DisplayPart part : model.getParts()) {
            org.joml.Vector3f translation = part.getTranslation();
            org.joml.Vector3f scale = part.getScale();

            // BlockDisplay renders from translation as the CORNER (0,0,0) to (scale, scale, scale).
            // It is NOT centered at translation — so the correct bounds are [translation, translation+scale].
            // Note: scale can be negative in some imported models, so we use min/max to handle both directions.
            double tx = translation.x;
            double ty = translation.y;
            double tz = translation.z;
            double sx = scale.x;
            double sy = scale.y;
            double sz = scale.z;

            minX = Math.min(minX, Math.min(tx, tx + sx));
            minY = Math.min(minY, Math.min(ty, ty + sy));
            minZ = Math.min(minZ, Math.min(tz, tz + sz));
            maxX = Math.max(maxX, Math.max(tx, tx + sx));
            maxY = Math.max(maxY, Math.max(ty, ty + sy));
            maxZ = Math.max(maxZ, Math.max(tz, tz + sz));
        }

        if (!Double.isFinite(minX)) {
            return defaultFootprint();
        }

        return fromBounds(minX, minY, minZ, maxX, maxY, maxZ);
    }

    static FurnitureFootprint fromFunction(FunctionFootprint footprint) {
        return new FurnitureFootprint(
                footprint.getCenterX(),
                footprint.getCenterY(),
                footprint.getCenterZ(),
                Math.max(0.45, footprint.getWidth()),
                Math.max(0.45, footprint.getHeight()),
                Math.max(0.45, footprint.getDepth())
        );
    }

    static FurnitureFootprint defaultFootprint() {
        return new FurnitureFootprint(0, 0.5, 0, 1, 1, 1);
    }

    private static FurnitureFootprint fromBounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new FurnitureFootprint(
                (minX + maxX) * 0.5,
                (minY + maxY) * 0.5,
                (minZ + maxZ) * 0.5,
                // Lower minimum from 0.6 → 0.2 so small/thin models get a tight hitbox
                Math.max(0.2, maxX - minX),
                Math.max(0.2, maxY - minY),
                Math.max(0.2, maxZ - minZ)
        );
    }

    double bottomY() {
        return centerY - (height * 0.5);
    }
}
