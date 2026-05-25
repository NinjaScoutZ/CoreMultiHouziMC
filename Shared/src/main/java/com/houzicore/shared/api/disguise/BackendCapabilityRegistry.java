package com.houzicore.shared.api.disguise;

/**
 * Resolves the appropriate backend for a given disguise request.
 * Allows decoupling the routing logic in RoutingDisguiseService from hardcoded dependencies.
 */
public interface BackendCapabilityRegistry {

    /**
     * Finds the most capable backend that supports the given request.
     *
     * @param request The disguise request to process.
     * @return The best backend to handle the request.
     */
    DisguiseBackend resolveBackend(DisguiseRequest request);
}
