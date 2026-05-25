package com.houzicore.shared.core.reward.rewards;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.reward.RewardRarity;

/**
 * Created by shaun on 14-09-12.
 */
@Deprecated
public class UnknownPackageReward extends CosmeticReward {
	public UnknownPackageReward(DonationManager donationManager, String name, String packageName, ItemStack itemStack,
			RewardRarity rarity, int weight) {
		super(donationManager, name, packageName, itemStack, rarity, weight);
	}
}
