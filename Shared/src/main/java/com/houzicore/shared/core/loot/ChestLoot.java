package com.houzicore.shared.core.loot;

import java.util.ArrayList;

import com.houzicore.shared.common.util.UtilMath;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ChestLoot {
	private final ArrayList<RandomItem> _randomItems = new ArrayList<>();
	private int _totalLoot;
	private final boolean _unbreakableLoot;

	public ChestLoot() {
		this(false);
	}

	public ChestLoot(boolean unbreakableLoot) {
		_unbreakableLoot = unbreakableLoot;
	}

	public void addLoot(ItemStack item, int amount) {
		addLoot(item, amount, item.getAmount(), item.getAmount());
	}

	public void addLoot(ItemStack item, int amount, int minStackSize, int maxStackSize) {
		addLoot(new RandomItem(item, amount, minStackSize, maxStackSize));
	}

	public void addLoot(Material material, int amount) {
		addLoot(material, amount, 1, 1);
	}

	public void addLoot(Material material, int amount, int minStackSize, int maxStackSize) {
		addLoot(new ItemStack(material), amount, minStackSize, maxStackSize);
	}

	public void addLoot(RandomItem item) {
		_totalLoot += item.getAmount();
		_randomItems.add(item);
	}

	public void cloneLoot(ChestLoot loot) {
		_totalLoot += loot._totalLoot;
		_randomItems.addAll(loot._randomItems);
	}

	public ItemStack getLoot() {
		int no = UtilMath.r(_totalLoot);

		for (final RandomItem item : _randomItems) {
			no -= item.getAmount();

			if (no < 0) {
				final ItemStack itemstack = item.getItemStack();

				if (_unbreakableLoot && itemstack.getType().getMaxDurability() > 16) {
					final ItemMeta meta = itemstack.getItemMeta();
					meta.setUnbreakable(true);
					itemstack.setItemMeta(meta);
				}

				return itemstack;
			}
		}

		return null;
	}
}
