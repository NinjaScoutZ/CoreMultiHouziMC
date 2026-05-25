package com.houzicore.shared.core.disguise.v2;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

import com.houzicore.shared.api.disguise.DisguiseRequest;
import com.houzicore.shared.api.disguise.DisguiseService;
import com.houzicore.shared.api.disguise.DisguiseSession;

public class RoutingDisguiseService implements DisguiseService {

    private final List<DisguiseBackendAdapter> adapters;
    private final Map<UUID, DisguiseSession> sessions = new ConcurrentHashMap<>();

    public RoutingDisguiseService(List<DisguiseBackendAdapter> adapters) {
        this.adapters = new ArrayList<>(adapters);
    }

    @Override
    public Optional<DisguiseSession> getActiveSession(UUID subjectId) {
        return Optional.ofNullable(sessions.get(subjectId));
    }

    @Override
    public DisguiseSession apply(LivingEntity target, DisguiseRequest request) {
        List<DisguiseBackendAdapter> candidates = adapters.stream()
                .filter(DisguiseBackendAdapter::isAvailable)
                .filter(adapter -> adapter.supports(request))
                .toList();

        if (candidates.isEmpty()) {
            throw new IllegalStateException("No disguise backend available for request " + request);
        }

        RuntimeException lastFailure = null;
        for (DisguiseBackendAdapter adapter : candidates) {
            try {
                adapter.apply(target, request);

                DisguiseSession session = new DisguiseSession(
                        target.getUniqueId(),
                        request,
                        adapter.getBackend(),
                        Instant.now());
                sessions.put(target.getUniqueId(), session);
                return session;
            } catch (RuntimeException ex) {
                lastFailure = ex;
                Bukkit.getLogger().warning("[DisguiseService] Backend " + adapter.getBackend()
                        + " failed for " + request.variantKey() + ": " + ex.getMessage());
            }
        }

        throw new IllegalStateException("All disguise backends failed for request " + request, lastFailure);
    }

    @Override
    public void clear(LivingEntity target) {
        DisguiseSession previous = sessions.remove(target.getUniqueId());
        if (previous == null) {
            return;
        }

        adapters.stream()
                .filter(adapter -> adapter.getBackend() == previous.backend())
                .findFirst()
                .ifPresent(adapter -> adapter.clear(target));
    }

    @Override
    public boolean supports(DisguiseRequest request) {
        return resolveAdapter(request).isPresent();
    }

    private Optional<DisguiseBackendAdapter> resolveAdapter(DisguiseRequest request) {
        return adapters.stream()
                .filter(DisguiseBackendAdapter::isAvailable)
                .filter(adapter -> adapter.supports(request))
                .findFirst();
    }
}
