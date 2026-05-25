package com.houzicore.shared.core.feature;

import com.houzicore.shared.api.context.ContextPolicyRegistry;
import com.houzicore.shared.api.context.PlayerContextId;
import com.houzicore.shared.api.context.PlayerContextService;
import com.houzicore.shared.api.feature.FeatureGate;
import com.houzicore.shared.api.feature.FeatureKey;

import org.bukkit.entity.Player;

public class ContextAwareFeatureGate implements FeatureGate {

    private final PlayerContextService contextService;
    private final ContextPolicyRegistry policyRegistry;

    public ContextAwareFeatureGate(PlayerContextService contextService, ContextPolicyRegistry policyRegistry) {
        this.contextService = contextService;
        this.policyRegistry = policyRegistry;
    }

    @Override
    public boolean isAllowed(Player player, FeatureKey featureKey) {
        PlayerContextId contextId = contextService.getCurrentContextId(player);
        return policyRegistry.find(contextId)
                .map(policy -> policy.isEnabled(featureKey))
                .orElse(false);
    }
}
