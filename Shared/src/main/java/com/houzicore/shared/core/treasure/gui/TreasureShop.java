package com.houzicore.shared.core.treasure.gui;

import org.bukkit.entity.Player;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.shared.core.treasure.TreasureInventoryService;
import com.houzicore.shared.core.treasure.TreasureLocation;
import com.houzicore.shared.core.treasure.TreasureManager;

public class TreasureShop extends ShopBase<TreasureManager> {
	private final TreasureLocation _treasureLocation;
	private final TreasureInventoryService _treasureInventoryService;

	public TreasureShop(TreasureManager plugin, TreasureInventoryService treasureInventoryService, CoreClientManager clientManager,
			DonationManager donationManager, TreasureLocation treasureLocation) {
		super(plugin, clientManager, donationManager, "Treasure Shop");
		_treasureLocation = treasureLocation;
		_treasureInventoryService = treasureInventoryService;
	}

	@Override
	protected ShopPageBase<TreasureManager, ? extends ShopBase<TreasureManager>> buildPagesFor(Player player) {
		return new TreasurePage(getPlugin(), this, _treasureLocation, getClientManager(), getDonationManager(),
				_treasureInventoryService, player);
	}
}
