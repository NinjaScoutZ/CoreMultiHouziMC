package com.houzicore.shared.core.shop.item;

import java.util.Arrays;
import java.util.List;

import com.houzicore.shared.account.CoreClient;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.InventoryUtil;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.lang.LangManager;
import org.bukkit.inventory.Inventory;

import org.bukkit.Material;
////import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftInventory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemPackage implements ISalesPackage {
	private final ShopItem _shopItem;
	private final boolean _restrictToHotbar;
	private final int _gemCost;
	private final boolean _free;
	private final int _salesPackageId;

	public ItemPackage(ShopItem shopItem, boolean restrictToHotbar, int gemCost, boolean isFree, int salesPackageId) {
		_shopItem = shopItem;
		_restrictToHotbar = restrictToHotbar;
		_gemCost = gemCost;
		_free = isFree;
		_salesPackageId = salesPackageId;
	}

	public ItemPackage(ShopItem shopItem, int gemCost, boolean isFree, int salesPackageId) {
		this(shopItem, true, gemCost, isFree, salesPackageId);
	}

	@Override
	public List<Integer> AddToCategory(Inventory inventory, int slot) {
		inventory.setItem(slot, _shopItem);

		return Arrays.asList(slot);
	}

	@Override
	public boolean CanFitIn(CoreClient player) {
		if (_shopItem.IsLocked() && !IsFree())
			return false;

		for (final ItemStack itemStack : player.GetPlayer().getInventory()) {
			if (itemStack != null && itemStack.getType() == _shopItem.getType() && itemStack.getAmount() + _shopItem
					.getAmount() <= (itemStack.getType() == Material.ARROW ? itemStack.getMaxStackSize() : 1))
				return true;
		}

		if (_gemCost == 0)
			return true;

		if (InventoryUtil.first(player.GetPlayer().getInventory(),
				_restrictToHotbar ? 9 : player.GetPlayer().getInventory().getSize(), null, true) == -1)
			return false;
		else
			return true;
	}

	@Override
	public void DeliverTo(Player player) {
		final ShopItem shopItem = _shopItem.clone();
		shopItem.SetDeliverySettings();

		if (shopItem.getType() == Material.ARROW) {
			// Arrows stack, use addItem which handles stacking and overflow
			if (player.getInventory().firstEmpty() == -1 && player.getInventory().all(Material.ARROW).isEmpty()) {
				UtilPlayer.message(player, F.main("Shop", LangManager.get().get(player, "shop.inventory_full")));
				return;
			}
			player.getInventory().addItem(shopItem);
		} else {
			final int emptySlot = player.getInventory().firstEmpty();
			if (emptySlot == -1) {
				UtilPlayer.message(player, F.main("Shop", LangManager.get().get(player, "shop.inventory_full")));
				return;
			}
			player.getInventory().setItem(emptySlot, shopItem);
		}
	}

	@Override
	public void DeliverTo(Player player, int slot) {
		final ShopItem shopItem = _shopItem.clone();
		shopItem.SetDeliverySettings();

		if (slot < 0 || slot >= player.getInventory().getSize()) {
			UtilPlayer.message(player, F.main("Shop", "Your inventory is full!"));
			return;
		}
		player.getInventory().setItem(slot, shopItem);
	}

	@Override
	public int GetGemCost() {
		return _gemCost;
	}

	public ShopItem GetItem() {
		return _shopItem;
	}

	@Override
	public String GetName() {
		return _shopItem.GetName();
	}

	@Override
	public int GetSalesPackageId() {
		return _salesPackageId;
	}

	@Override
	public boolean IsFree() {
		return _free;
	}

	@Override
	public void PurchaseBy(CoreClient player) {
		DeliverTo(player.GetPlayer());
	}

	@Override
	public int ReturnFrom(CoreClient player) {
		if (_shopItem.IsDisplay())
			return 0;

		final ShopItem shopItem = _shopItem.clone();
		shopItem.SetDeliverySettings();

		int count = 0;

		count = InventoryUtil.getCountOfObjectsRemoved(player.GetPlayer().getInventory(), 9,
				(ItemStack) shopItem);

		return count;
	}
}
