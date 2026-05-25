package com.houzicore.shared.core.reward.math;

import org.bukkit.entity.Player;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.core.reward.math.MultiplierEngine.MultiplierProvider;

public class RankMultiplierProvider implements MultiplierProvider {

    private final CoreClientManager _clientManager;

    public RankMultiplierProvider(CoreClientManager clientManager) {
        _clientManager = clientManager;
    }

    @Override
    public String getName() {
        return "Rank Bonus";
    }

    @Override
    public double getBonus(Player player) {
        if (_clientManager.Get(player) != null) {
            Rank rank = _clientManager.Get(player).GetRank();
            // getCoinMultiplier() returns things like 1.0, 1.25, 1.5, 2.0.
            // The bonus should be the additive portion (e.g. 1.25 -> 0.25 bonus)
            double multiplier = rank.getCoinMultiplier();
            if (multiplier > 1.0) {
                return multiplier - 1.0;
            }
        }
        return 0.0;
    }
}
