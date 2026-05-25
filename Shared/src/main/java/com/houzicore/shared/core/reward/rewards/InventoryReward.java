package com.houzicore.shared.core.reward.rewards;

import java.util.Random;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.core.inventory.InventoryManager;
import com.houzicore.shared.core.reward.Reward;
import com.houzicore.shared.core.reward.RewardData;
import com.houzicore.shared.core.reward.RewardRarity;

/**
 * Created by shaun on 14-09-12.
 */
public class InventoryReward extends Reward {
	private final Random _random;

	private final InventoryManager _inventoryManager;
	private final ItemStack _itemStack;
	private final String _name;
	private final String _packageName;
	private final int _minAmount;
	private final int _maxAmount;

	public InventoryReward(InventoryManager inventoryManager, String name, String packageName, int minAmount,
			int maxAmount, ItemStack itemStack, RewardRarity rarity, int weight) {
		this(RANDOM, inventoryManager, name, packageName, minAmount, maxAmount, itemStack, rarity, weight);
	}

	public InventoryReward(Random random, InventoryManager inventoryManager, String name, String packageName,
			int minAmount, int maxAmount, ItemStack itemStack, RewardRarity rarity, int weight) {
		super(rarity, weight);

		_random = random;
		_name = name;
		_packageName = packageName;
		_minAmount = minAmount;
		_maxAmount = maxAmount;
		_itemStack = itemStack;
		_inventoryManager = inventoryManager;
	}

	@Override
	public boolean canGiveReward(Player player) {
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof InventoryReward)
			return ((InventoryReward) obj).getPackageName().equals(_packageName);
		return false;
	}

	protected String getPackageName() {
		return _packageName;
	}

	@Override
	public RewardData giveRewardCustom(Player player) {
		int amountToGive;

		if (_minAmount != _maxAmount) {
			amountToGive = _random.nextInt(_maxAmount - _minAmount) + _minAmount;
		} else {
			amountToGive = _minAmount;
		}

		_inventoryManager.addItemToInventory(player, "Item", _packageName, amountToGive);

		return new RewardData(getRarity().getColor() + amountToGive + " " + _name, _itemStack);
	}

	@Override
	public RewardData getFakeRewardData(Player player) {
		int amountToGive = (_maxAmount + _minAmount) / 2;
		if (amountToGive == 0) amountToGive = _minAmount;
		return new RewardData(getRarity().getColor() + amountToGive + " " + _name, _itemStack);
	}
}
