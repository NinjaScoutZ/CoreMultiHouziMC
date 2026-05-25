package com.houzicore.mapbuilder.session;

import com.houzicore.mapbuilder.domain.MapPointDefinition;
import com.houzicore.mapbuilder.template.MapTemplate;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Per-session mutable state that is completely separate from the map data
 * stored in MapSession.  Owns:
 *
 *   • Point Tool selection  (never shared with Display Tool)
 *   • Display Tool selection
 *   • PAIR_REGION pending anchor
 *   • Undo / Redo stacks
 *   • The MapTemplate governing this session
 */
public final class BuilderSessionState {

    private static final int MAX_HISTORY = 64;

    // ── Template ──────────────────────────────────────────────────────────
    private final MapTemplate template;

    // ── Point Tool ─────────────────────────────────────────────────────────
    private MapPointDefinition selectedPoint = null;

    // ── Display Tool ────────────────────────────────────────────────────────
    private Material selectedDisplayMaterial = null;
    private String   selectedDisplayModel    = null;

    // ── PAIR_REGION pending anchor ───────────────────────────────────────────
    private MapPointDefinition pendingRegionType   = null;
    private Location           pendingRegionAnchor = null;

    // ── Undo / Redo ─────────────────────────────────────────────────────────
    private final Deque<PlacementAction> undoStack = new ArrayDeque<>();
    private final Deque<PlacementAction> redoStack = new ArrayDeque<>();

    // ─────────────────────────────────────────────────────────────────────────

    public BuilderSessionState(MapTemplate template) {
        this.template = template;
    }

    // ── Template ──────────────────────────────────────────────────────────
    public MapTemplate getTemplate() { return template; }

    // ── Point Tool selection ───────────────────────────────────────────────
    public MapPointDefinition getSelectedPoint()               { return selectedPoint; }
    public boolean hasSelectedPoint()                          { return selectedPoint != null; }
    public void selectPoint(MapPointDefinition def)            { selectedPoint = def; }
    public void deselectPoint()                                { selectedPoint = null; }

    // ── Display Tool selection ─────────────────────────────────────────────
    public Material getSelectedDisplayMaterial()                    { return selectedDisplayMaterial; }
    public void     selectDisplayMaterial(Material mat)             { selectedDisplayMaterial = mat; selectedDisplayModel = null; }
    public String   getSelectedDisplayModel()                       { return selectedDisplayModel; }
    public void     selectDisplayModel(String modelId)              { selectedDisplayModel = modelId; selectedDisplayMaterial = null; }
    public void     deselectDisplay()                               { selectedDisplayMaterial = null; selectedDisplayModel = null; }
    public boolean  hasDisplaySelection()                           { return selectedDisplayMaterial != null || selectedDisplayModel != null; }

    // ── PAIR_REGION state ──────────────────────────────────────────────────
    public boolean hasPendingRegionAnchor()                         { return pendingRegionAnchor != null; }
    public MapPointDefinition getPendingRegionType()                 { return pendingRegionType; }
    public Location           getPendingRegionAnchor()               { return pendingRegionAnchor; }
    public void setPendingRegion(MapPointDefinition type, Location anchor) {
        this.pendingRegionType   = type;
        this.pendingRegionAnchor = anchor.clone();
    }
    public void clearPendingRegion() {
        pendingRegionType   = null;
        pendingRegionAnchor = null;
    }

    // ── Undo / Redo ─────────────────────────────────────────────────────────

    public void recordAction(PlacementAction action) {
        undoStack.push(action);
        if (undoStack.size() > MAX_HISTORY) {
            // Trim oldest (bottom of deque):
            ((ArrayDeque<PlacementAction>) undoStack).removeLast();
        }
        redoStack.clear(); // a new action invalidates the redo branch
    }

    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }

    /** Pop the most recent action for the caller to reverse. */
    public PlacementAction popUndo() {
        PlacementAction action = undoStack.pop();
        redoStack.push(action);
        return action;
    }

    /** Pop the most recent undone action for the caller to re-apply. */
    public PlacementAction popRedo() {
        PlacementAction action = redoStack.pop();
        undoStack.push(action);
        return action;
    }

    public void clear() {
        selectedPoint          = null;
        selectedDisplayMaterial= null;
        selectedDisplayModel   = null;
        pendingRegionType      = null;
        pendingRegionAnchor    = null;
        undoStack.clear();
        redoStack.clear();
    }
}
