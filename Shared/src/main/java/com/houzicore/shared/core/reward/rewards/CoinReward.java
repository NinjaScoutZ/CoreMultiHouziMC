package com.houzicore.shared.core.reward.rewards;

import java.util.Random;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.reward.Reward;
import com.houzicore.shared.core.reward.RewardData;
import com.houzicore.shared.core.reward.RewardRarity;

public class CoinReward extends Reward {
	private final DonationManager _donationManager;
	private final Random _random;
	private final int _minCoinCount;
	private final int _maxCoinCount;

	public CoinReward(DonationManager donationManager, int minCoinCount, int maxCoinCount, int weight,
			RewardRarity rarity) {
		this(donationManager, minCoinCount, maxCoinCount, weight, rarity, RANDOM);
	}

	public CoinReward(DonationManager donationManager, int minCoinCount, int maxCoinCount, int weight,
			RewardRarity rarity, Random random) {
		super(rarity, weight);
		_donationManager = donationManager;
		_minCoinCount = minCoinCount;
		_maxCoinCount = maxCoinCount;

		_random = random;
	}

	@Override
	public boolean canGiveReward(Player player) {
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CoinReward)
			return true;

		return false;
	}

	@Override
	public RewardData giveRewardCustom(Player player) {
		final int gemsToReward = _random.nextInt(_maxCoinCount - _minCoinCount) + _minCoinCount;

		_donationManager.RewardCoins(new Callback<Boolean>() {
			@Override
			public void run(Boolean data) {

			}
		}, "Treasure Chest", player.getName(), _donationManager.getClientManager().Get(player).getAccountId(),
				gemsToReward);

		return new RewardData(getRarity().getColor() + gemsToReward + " Coins", new ItemStack(org.bukkit.Material.SUNFLOWER));
	}

	@Override
	public RewardData getFakeRewardData(Player player) {
		int gemsToReward = (_maxCoinCount + _minCoinCount) / 2;
		return new RewardData(getRarity().getColor() + gemsToReward + " Coins", new ItemStack(org.bukkit.Material.SUNFLOWER));
	}
}
