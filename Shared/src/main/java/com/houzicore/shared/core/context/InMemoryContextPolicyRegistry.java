package com.houzicore.shared.core.context;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import com.houzicore.shared.api.context.ContextPolicy;
import com.houzicore.shared.api.context.ContextPolicyRegistry;
import com.houzicore.shared.api.context.PlayerContextId;

public class InMemoryContextPolicyRegistry implements ContextPolicyRegistry {

    private final Map<PlayerContextId, ContextPolicy> policies = new EnumMap<>(PlayerContextId.class);

    @Override
    public void register(ContextPolicy policy) {
        policies.put(policy.contextId(), policy);
    }

    @Override
    public Optional<ContextPolicy> find(PlayerContextId contextId) {
        return Optional.ofNullable(policies.get(contextId));
    }
}
