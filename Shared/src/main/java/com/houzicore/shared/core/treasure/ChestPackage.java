package com.houzicore.shared.core.treasure;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.houzicore.shared.common.CurrencyType;
import com.houzicore.shared.core.shop.item.SalesPackageBase;

public class ChestPackage extends SalesPackageBase {
	public ChestPackage(String name, Material mat, int cost) {
		super(name, mat, (byte) 0, new String[] {}, cost);

		KnownPackage = false;
		OneTimePurchaseOnly = false;
	}

	@Override
	public void Sold(Player player, CurrencyType currencyType) {

	}
}
