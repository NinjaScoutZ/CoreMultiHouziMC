package com.houzicore.shared.core.displayentity.furniture;

import java.util.ArrayList;
import java.util.List;

import com.houzicore.shared.core.displayentity.DisplayModel;
import com.houzicore.shared.core.displayentity.DisplayPart;

/**
 * Scans a {@link DisplayModel}'s constituent parts and automatically generates
 * tight-fitting Interaction zones that conform to the model's actual geometry.
 * <p>
 * Instead of wrapping the entire model in one oversized bounding box, this
 * scanner computes the AABB (axis-aligned bounding box) of each DisplayPart,
 * then merges overlapping or adjacent boxes into clusters. Each cluster becomes
 * one {@link InteractionZone} — a position + radius + height for an Interaction entity.
 * <p>
 * This is the "Auto Perfect Hitbox" system: models of any shape (L-shaped tables,
 * tall lamps, multi-piece chairs) get multiple tight hitboxes automatically,
 * with no manual YAML configuration needed.
 */
final class AutoHitboxScanner {

    /** Maximum number of Interaction entities per furniture piece to avoid entity spam. */
    private static final int MAX_ZONES = 8;

    /** Boxes closer than this (in blocks) will be merged into one zone. */
    private static final double MERGE_THRESHOLD = 0.35;

    /** Minimum zone radius (blocks). */
    private static final float MIN_RADIUS = 0.12f;

    /** Maximum zone radius (blocks). */
    private static final float MAX_RADIUS = 3.0f;

    /** Minimum zone height (blocks). */
    private static final float MIN_HEIGHT = 0.12f;

    /** Maximum zone height (blocks). */
    private static final float MAX_HEIGHT = 5.0f;

    private AutoHitboxScanner() {}

    // ── Public API ──────────────────────────────────────

    /**
     * A single auto-generated Interaction zone.
     * <p>
     * {@code offsetX/Y/Z} are local-space offsets from the model origin.
     * {@code width} is the Interaction entity radius (half-diameter cylinder).
     * {@code height} is the Interaction entity height (grows upward from offsetY).
     */
    static final class InteractionZone {
        final double offsetX, offsetY, offsetZ;
        final float width;   // Interaction entity "width" = radius of its cylinder
        final float height;

        InteractionZone(double ox, double oy, double oz, float w, float h) {
            offsetX = ox;
            offsetY = oy;
            offsetZ = oz;
            width = w;
            height = h;
        }

        @Override
        public String toString() {
            return String.format("InteractionZone[offset=(%.3f,%.3f,%.3f) radius=%.3f height=%.3f]",
                    offsetX, offsetY, offsetZ, width, height);
        }
    }

    /**
     * Scan a model and produce tight-fitting interaction zones.
     * <p>
     * Algorithm:
     * <ol>
     *   <li>Compute the AABB of each {@link DisplayPart}.</li>
     *   <li>Greedily merge overlapping / nearby boxes (gap ≤ {@link #MERGE_THRESHOLD}).</li>
     *   <li>If still too many clusters, force-merge the closest pairs until ≤ {@link #MAX_ZONES}.</li>
     *   <li>Convert each merged AABB to an {@link InteractionZone}.</li>
     * </ol>
     *
     * @param model The model to scan
     * @return Non-empty list of interaction zones
     */
    static List<InteractionZone> scan(DisplayModel model) {
        List<DisplayPart> parts = model.getParts();
        if (parts.isEmpty()) {
            return List.of(new InteractionZone(0, 0, 0, 0.25f, 0.25f));
        }

        // Step 1 — Compute AABB per part
        List<AABB> boxes = new ArrayList<>(parts.size());
        for (DisplayPart part : parts) {
            boxes.add(computeAABB(part));
        }

        // Step 2 — Greedy proximity merge (gap-based)
        List<AABB> merged = greedyMerge(boxes, MERGE_THRESHOLD);

        // Step 3 — Cap entity count by force-merging closest pairs
        while (merged.size() > MAX_ZONES) {
            merged = forceClosestMerge(merged);
        }

        // Step 4 — Convert to InteractionZones
        List<InteractionZone> zones = new ArrayList<>(merged.size());
        for (AABB box : merged) {
            zones.add(toZone(box));
        }

        return zones;
    }

    // ── AABB helpers ────────────────────────────────────

    /**
     * Compute the local-space AABB for a single DisplayPart.
     * BlockDisplay entities render from their translation corner to translation + scale.
     */
    private static AABB computeAABB(DisplayPart part) {
        org.joml.Vector3f t = part.getTranslation();
        org.joml.Vector3f s = part.getScale();

        // Handle negative scales (mirroring)
        double x1 = t.x, x2 = t.x + s.x;
        double y1 = t.y, y2 = t.y + s.y;
        double z1 = t.z, z2 = t.z + s.z;

        return new AABB(
                Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2),
                Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2)
        );
    }

    /**
     * Greedily merge boxes whose gap is at most {@code threshold}.
     * Repeats passes until no more merges are possible.
     */
    private static List<AABB> greedyMerge(List<AABB> boxes, double threshold) {
        List<AABB> result = new ArrayList<>(boxes);
        boolean changed = true;
        while (changed) {
            changed = false;
            outer:
            for (int i = 0; i < result.size(); i++) {
                for (int j = i + 1; j < result.size(); j++) {
                    if (result.get(i).gapTo(result.get(j)) <= threshold) {
                        AABB combined = result.get(i).union(result.get(j));
                        result.set(i, combined);
                        result.remove(j);
                        changed = true;
                        break outer;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Force-merge the two closest boxes (by gap distance).
     */
    private static List<AABB> forceClosestMerge(List<AABB> boxes) {
        int bestI = 0, bestJ = 1;
        double bestGap = Double.MAX_VALUE;
        for (int i = 0; i < boxes.size(); i++) {
            for (int j = i + 1; j < boxes.size(); j++) {
                double gap = boxes.get(i).gapTo(boxes.get(j));
                if (gap < bestGap) {
                    bestGap = gap;
                    bestI = i;
                    bestJ = j;
                }
            }
        }
        List<AABB> result = new ArrayList<>(boxes);
        AABB combined = result.get(bestI).union(result.get(bestJ));
        result.set(bestI, combined);
        result.remove(bestJ);
        return result;
    }

    /**
     * Convert a merged AABB into an Interaction entity zone.
     * <p>
     * Interaction entity is a vertical cylinder:
     * <ul>
     *   <li>{@code width} = radius (half-diameter)</li>
     *   <li>{@code height} = grows upward from spawn Y</li>
     *   <li>Position = bottom-center of the cylinder</li>
     * </ul>
     */
    private static InteractionZone toZone(AABB box) {
        double centerX = (box.minX + box.maxX) * 0.5;
        double bottomY = box.minY;
        double centerZ = (box.minZ + box.maxZ) * 0.5;

        double sizeX = box.maxX - box.minX;
        double sizeY = box.maxY - box.minY;
        double sizeZ = box.maxZ - box.minZ;

        // Interaction width = radius of cylinder. Use the larger horizontal extent / 2 + tiny padding.
        float radius = clampF((float) (Math.max(sizeX, sizeZ) * 0.5 + 0.03), MIN_RADIUS, MAX_RADIUS);
        float height = clampF((float) (sizeY + 0.03), MIN_HEIGHT, MAX_HEIGHT);

        return new InteractionZone(centerX, bottomY, centerZ, radius, height);
    }

    private static float clampF(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    // ── AABB record ─────────────────────────────────────

    /**
     * Axis-aligned bounding box in local model space.
     */
    private static final class AABB {
        final double minX, minY, minZ, maxX, maxY, maxZ;

        AABB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }

        /** Return the union (smallest box enclosing both). */
        AABB union(AABB o) {
            return new AABB(
                    Math.min(minX, o.minX), Math.min(minY, o.minY), Math.min(minZ, o.minZ),
                    Math.max(maxX, o.maxX), Math.max(maxY, o.maxY), Math.max(maxZ, o.maxZ)
            );
        }

        /**
         * Compute the minimum gap between two AABBs.
         * Returns 0 if they overlap or touch.
         * Uses Chebyshev distance (max-axis gap) so diagonal neighbours still merge.
         */
        double gapTo(AABB o) {
            double gapX = Math.max(0, Math.max(minX - o.maxX, o.minX - maxX));
            double gapY = Math.max(0, Math.max(minY - o.maxY, o.minY - maxY));
            double gapZ = Math.max(0, Math.max(minZ - o.maxZ, o.minZ - maxZ));
            return Math.max(gapX, Math.max(gapY, gapZ));
        }
    }
}
