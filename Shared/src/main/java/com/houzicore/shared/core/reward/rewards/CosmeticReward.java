package com.houzicore.shared.core.reward.rewards;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.reward.Reward;
import com.houzicore.shared.core.reward.RewardData;
import com.houzicore.shared.core.reward.RewardRarity;

public class CosmeticReward extends Reward {
	private final DonationManager _donationManager;
	private final ItemStack _itemStack;
	private final String _displayName;
	private final String _unlockId;

	public CosmeticReward(DonationManager donationManager, String displayName, String unlockId, ItemStack itemStack,
			RewardRarity rarity, int weight) {
		super(rarity, weight);
		_donationManager = donationManager;
		_displayName = displayName;
		_unlockId = unlockId;
		_itemStack = itemStack;
	}

	@Override
	public boolean canGiveReward(Player player) {
		if (_donationManager.Get(player.getName()) == null) {
			return false;
		}

		return !_donationManager.Get(player.getName()).OwnsUnknownPackage(_unlockId);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CosmeticReward reward) {
			return reward.getUnlockId().equals(_unlockId);
		}
		return false;
	}

	protected String getDisplayName() {
		return _displayName;
	}

	protected String getUnlockId() {
		return _unlockId;
	}

	@Override
	protected RewardData giveRewardCustom(Player player) {
		_donationManager.PurchaseUnknownSalesPackage(null, player.getName(),
				_donationManager.getClientManager().Get(player).getAccountId(), _unlockId, true, 0, true);

		return new RewardData(getRarity().getColor() + _displayName, _itemStack);
	}

	@Override
	public RewardData getFakeRewardData(Player player) {
		return new RewardData(getRarity().getColor() + _displayName, _itemStack);
	}
}
