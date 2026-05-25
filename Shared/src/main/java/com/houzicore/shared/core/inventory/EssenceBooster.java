package com.houzicore.shared.core.inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.houzicore.shared.common.CurrencyType;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.shop.item.SalesPackageBase;

public class EssenceBooster extends SalesPackageBase {
	public EssenceBooster(boolean enabled, int gemBoosters) {
		super("20 Essence Booster Pack", Material.EMERALD, (byte) 0,
				new String[] { C.cYellow + "1000 Coins", " ", enabled ? C.cGreen + "Left-Click To Use:" : "",
						C.cWhite + "Use these before games start to", C.cWhite + "boost the amount of Essence earned",
						C.cWhite + "for all players in the game!", " ", C.cGreen + "Right-Click To Purchase:",
						C.cWhite + "20 Essence Boosters for " + C.cYellow + "1000 Coins", " ",
						C.cWhite + "Your Essence Boosters: " + C.cGreen + gemBoosters },
				1000, 20);

		KnownPackage = false;
		OneTimePurchaseOnly = false;
	}

	@Override
	public void Sold(Player player, CurrencyType currencyType) {
	}
}
