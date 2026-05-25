package com.houzicore.shared.core.treasure.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.common.CurrencyType;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.shared.core.shop.page.ConfirmationPage;
import com.houzicore.shared.core.treasure.ChestPackage;
import com.houzicore.shared.core.treasure.TreasureInventoryService;
import com.houzicore.shared.core.treasure.TreasureManager;
import com.houzicore.shared.core.treasure.TreasureType;

public class BuyChestButton implements IButton {
	private final TreasureInventoryService _treasureInventoryService;

	private final TreasurePage _page;

	private final TreasureType _treasureType;
	private final Material _chestMat;
	private final int _chestCost;

	public BuyChestButton(Player player, TreasureInventoryService treasureInventoryService, TreasurePage page, TreasureType treasureType,
			Material chestMat, int chestCost) {
		_treasureInventoryService = treasureInventoryService;

		_page = page;

		_treasureType = treasureType;
		_chestMat = chestMat;
		_chestCost = chestCost;
	}

	@Override
	public void onClick(final Player player, ClickType clickType) {
		player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.6f);
		_page.getShop().openPageForPlayer(player, new ConfirmationPage<>(_page.getPlugin(), _page.getShop(),
				_page.getClientManager(), _page.getDonationManager(), new Runnable() {
					@Override
					public void run() {
						_treasureInventoryService.addChests(player, _treasureType, 1);
						_page.refresh();
					}
				}, _page, new ChestPackage(_treasureType.getPlainName(player), _chestMat, _chestCost), CurrencyType.Coins, player));
	}
}
