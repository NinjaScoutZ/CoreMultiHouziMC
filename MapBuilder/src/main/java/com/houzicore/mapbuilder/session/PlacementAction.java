package com.houzicore.mapbuilder.session;

import com.houzicore.mapbuilder.domain.MapPointDefinition;
import org.bukkit.Location;

/**
 * Immutable record of a single reversible editing action.
 * Used by BuilderSessionState's undo and redo stacks.
 */
public final class PlacementAction {

    public enum ActionType {
        PLACE,        // a data point was added
        DELETE,       // a data point was removed
        SET_MIN,      // boundary min corner was set
        SET_MAX       // boundary max corner was set
    }

    private final ActionType type;
    private final MapPointDefinition definition; // null for boundary/display actions
    private final String rawKey;                 // non-null when definition==null (display items)
    private final Location location;
    private final Location previousLocation;     // only used for SET_MIN / SET_MAX

    // ── PLACE / DELETE with definition ─────────────────────────────────────
    public PlacementAction(ActionType type, MapPointDefinition definition, Location location) {
        this.type             = type;
        this.definition       = definition;
        this.rawKey           = definition != null ? definition.exportKey : null;
        this.location         = location.clone();
        this.previousLocation = null;
    }

    // ── PLACE / DELETE for display items (rawKey e.g. "BLOCK_DISPLAY:STONE") ──
    public PlacementAction(ActionType type, String rawKey, Location location) {
        this.type             = type;
        this.definition       = null;
        this.rawKey           = rawKey;
        this.location         = location.clone();
        this.previousLocation = null;
    }

    // ── BOUNDARY ───────────────────────────────────────────────────────────
    public PlacementAction(ActionType type, Location newLocation, Location previousLocation) {
        this.type             = type;
        this.definition       = null;
        this.rawKey           = null;
        this.location         = newLocation != null ? newLocation.clone() : null;
        this.previousLocation = previousLocation != null ? previousLocation.clone() : null;
    }

    public ActionType           getType()            { return type; }
    public MapPointDefinition   getDefinition()      { return definition; }
    /** The export key string — valid for both catalog points and raw display keys. */
    public String               getRawKey()          { return rawKey; }
    public Location             getLocation()        { return location; }
    public Location             getPreviousLocation(){ return previousLocation; }
}
