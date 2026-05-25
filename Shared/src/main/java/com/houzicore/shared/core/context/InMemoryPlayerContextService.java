package com.houzicore.shared.core.context;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import com.houzicore.shared.api.context.PlayerContext;
import com.houzicore.shared.api.context.PlayerContextId;
import com.houzicore.shared.api.context.PlayerContextService;
import com.houzicore.shared.api.context.TransitionReason;

public class InMemoryPlayerContextService implements PlayerContextService {

    private final Map<UUID, PlayerContext> contexts = new ConcurrentHashMap<>();
    private final PlayerContextId defaultContextId;

    public InMemoryPlayerContextService(PlayerContextId defaultContextId) {
        this.defaultContextId = defaultContextId;
    }

    @Override
    public Optional<PlayerContext> getCurrentContext(Player player) {
        return Optional.ofNullable(contexts.get(player.getUniqueId()));
    }

    @Override
    public PlayerContextId getCurrentContextId(Player player) {
        return getCurrentContext(player)
                .map(PlayerContext::contextId)
                .orElse(defaultContextId);
    }

    @Override
    public PlayerContext transition(Player player, PlayerContextId target, TransitionReason reason) {
        PlayerContext context = new PlayerContext(player.getUniqueId(), target, reason, Instant.now());
        contexts.put(player.getUniqueId(), context);
        return context;
    }

    @Override
    public boolean isInContext(Player player, PlayerContextId contextId) {
        return getCurrentContextId(player) == contextId;
    }

    @Override
    public void cleanup(UUID playerId) {
        contexts.remove(playerId);
    }
}
