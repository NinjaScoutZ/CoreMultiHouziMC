package com.houzicore.arcade.nautilus.game.arcade.game.modules.chest;

import com.houzicore.shared.common.util.UtilMath;
import org.bukkit.inventory.ItemStack;

public class ChestLootItem
{

	private ItemStack _item;
	private int _lowestAmount, _highestAmount;

	ChestLootItem(ItemStack item, int lowestAmount, int highestAmount)
	{
		_item = item;
		_lowestAmount = lowestAmount;
		_highestAmount = highestAmount;
	}

	public ItemStack getItem()
	{
		ItemStack itemStack = _item.clone();

		if (_lowestAmount != _highestAmount)
		{
			itemStack.setAmount(_lowestAmount + UtilMath.r(_highestAmount - _lowestAmount + 1));
		}

		return itemStack;
	}
}
