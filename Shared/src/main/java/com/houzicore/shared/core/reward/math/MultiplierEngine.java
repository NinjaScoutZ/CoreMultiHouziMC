package com.houzicore.shared.core.reward.math;

import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class MultiplierEngine {

    private static final List<MultiplierProvider> _providers = new ArrayList<>();

    public static void registerProvider(MultiplierProvider provider) {
        if (!_providers.contains(provider)) {
            _providers.add(provider);
        }
    }

    public static void unregisterProvider(MultiplierProvider provider) {
        _providers.remove(provider);
    }

    public static java.util.Map<String, Double> describeMultipliers(Player player) {
        java.util.Map<String, Double> map = new java.util.LinkedHashMap<>();
        for (MultiplierProvider provider : _providers) {
            double bonus = provider.getBonus(player);
            if (bonus > 0) {
                map.put(provider.getName(), bonus);
            }
        }
        return map;
    }

    public static double evaluateGlobalMultiplier(Player player) {
        double totalMultiplier = 1.0;
        for (MultiplierProvider provider : _providers) {
            totalMultiplier += provider.getBonus(player);
        }
        return totalMultiplier;
    }

    public interface MultiplierProvider {
        String getName();
        
        /**
         * Calculates additive bonus multiplier. 
         * Return 0.0 for no bonus, 0.5 for +50%, 1.0 for +100%.
         */
        double getBonus(Player player);
    }
}
