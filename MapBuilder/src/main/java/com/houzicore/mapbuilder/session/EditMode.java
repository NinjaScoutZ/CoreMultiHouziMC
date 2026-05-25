package com.houzicore.mapbuilder.session;

/**
 * Defines the operational mode of a MapBuilder session.
 */
public enum EditMode {
    /**
     * Editing the current active world (e.g., Lobby, Hub).
     * Directly modifies the WorldConfig.dat in the current world folder.
     */
    CURRENT_WORLD,

    /**
     * Editing a packaged map from the Maps/ directory.
     * Usually involves a temporary world and zipping/unzipping.
     */
    PACKAGED_MAP,

    /**
     * Starting from a blank canvas in a sandbox environment.
     */
    SANDBOX
}
