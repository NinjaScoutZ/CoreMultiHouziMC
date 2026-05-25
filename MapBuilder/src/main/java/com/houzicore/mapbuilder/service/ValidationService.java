package com.houzicore.mapbuilder.service;

import com.houzicore.mapbuilder.MapSession;
import com.houzicore.mapbuilder.domain.MapPointDefinition;
import com.houzicore.mapbuilder.domain.PlacementKind;
import com.houzicore.mapbuilder.template.MapTemplate;
import org.bukkit.ChatColor;

public class ValidationService {

    /**
     * Evaluates a MapSession against its template and specific game logic rules.
     * Returns a detailed report of errors (blocking) and warnings (non-blocking).
     */
    public static ValidationReport validate(MapSession session) {
        ValidationReport report = new ValidationReport();
        MapTemplate template = session.getState().getTemplate();

        // 1. Boundary check
        if (template.requiresBoundary()) {
            if (session.getMinBoundary() == null || session.getMaxBoundary() == null) {
                report.addError("Boundary not set (Min + Max required). Use Boundary tool (Slot 2).");
            }
        }

        // 2. Required points & max points
        for (MapPointDefinition def : template.getAllPoints()) {
            int count = session.countPoints(def.exportKey);
            boolean isRequired = template.getRequiredPoints().contains(def);
            int minRequired = Math.max(def.minCount, isRequired ? (def.kind == PlacementKind.PAIR_REGION ? 2 : 1) : 0);
            
            if (count < minRequired) {
                report.addError(def.displayName + " — need ≥" + minRequired + ", have " + count);
            }
            
            if (def.maxCount != -1 && count > def.maxCount) {
                report.addWarning(def.displayName + " — have " + count + ", but recommended max is " + def.maxCount);
            }

            // 3. PAIR_REGION must be even
            if (def.kind == PlacementKind.PAIR_REGION) {
                if (count % 2 != 0) {
                    report.addError(def.displayName + " — has " + count + " point(s), must be an even number to form pairs.");
                }
            }
        }

        // 4. Custom Arena Logic Verification
        boolean hasArenaCenter = session.getDataPoints().containsKey("DATA_NAME:ZONE_ARENA");
        boolean hasSpawnA      = session.getDataPoints().containsKey("DATA_NAME:ARENA_SPAWN_A");
        boolean hasSpawnB      = session.getDataPoints().containsKey("DATA_NAME:ARENA_SPAWN_B");

        if (hasArenaCenter && (!hasSpawnA || !hasSpawnB)) {
            report.addWarning("You placed an Arena Center, but missing Spawn A and/or Spawn B. The system will fallback to offset ±5 from the center. It is recommended to place them explicitly.");
        }

        return report;
    }
}
