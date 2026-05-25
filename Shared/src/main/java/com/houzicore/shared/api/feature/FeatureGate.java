package com.houzicore.shared.api.feature;

import org.bukkit.entity.Player;

public interface FeatureGate {

    boolean isAllowed(Player player, FeatureKey featureKey);
}
