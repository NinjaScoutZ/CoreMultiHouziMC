package com.houzicore.shared.core.reward.pipeline;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.stats.StatsManager;

public class EssenceReward extends RewardBase {

    private final DonationManager _donationManager;
    private final StatsManager _statsManager;
    private final String _gameName;

    public EssenceReward(String reason, double amount, DonationManager donationManager, StatsManager statsManager, String gameName) {
        super(reason, amount);
        _donationManager = donationManager;
        _statsManager = statsManager;
        _gameName = gameName;
    }

    @Override
    public boolean supportsMultiplier() {
        return true;
    }

    @Override
    public void giveReward(Player player, double multiplier) {
        int finalAmount = (int) getCalculatedAmount(multiplier);
        if (finalAmount <= 0) return;

        _donationManager.RewardEssence(null, "Earned " + _gameName + " (" + getReason() + ")", player.getName(), player.getUniqueId(), finalAmount);
        
        if (_statsManager != null) {
            _statsManager.incrementStat(player, "Global.GemsEarned", finalAmount);
            _statsManager.incrementStat(player, _gameName + ".GemsEarned", finalAmount);
        }
    }

    @Override
    public void playAnimation(Player player, Location location) {
        if (location == null) location = player.getLocation().add(0, 1, 0);
        player.playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
        UtilParticle.PlayParticle(UtilParticle.ParticleType.HAPPY_VILLAGER, location, 0.5f, 0.5f, 0.5f, 0.1f, 10, UtilParticle.ViewDist.NORMAL, player);
    }

    @Override
    public String getSummaryString(double multiplier) {
        int finalAmount = (int) getCalculatedAmount(multiplier);
        return "  " + C.cAqua + "▸ " + C.cWhite + "+" + finalAmount + " \u00A77Essence  \u00A78• \u00A7e" + getReason();
    }
}
