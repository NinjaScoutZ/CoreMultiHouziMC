package com.houzicore.shared.core.booster;

import org.bukkit.entity.Player;
import com.houzicore.shared.core.reward.math.MultiplierEngine.MultiplierProvider;

public class GlobalBoosterProvider implements MultiplierProvider {

    private final BoosterManager _boosterManager;

    public GlobalBoosterProvider(BoosterManager boosterManager) {
        _boosterManager = boosterManager;
    }

    @Override
    public String getName() {
        return "Global Booster";
    }

    @Override
    public double getBonus(Player player) {
        if (_boosterManager.isBoosterActive()) {
            return 1.0; // +100% (x2 multiplier)
        }
        return 0.0;
    }
}
