package com.houzicore.shared.api.context;

import java.util.Optional;

public interface ContextPolicyRegistry {

    void register(ContextPolicy policy);

    Optional<ContextPolicy> find(PlayerContextId contextId);
}
