package com.houzicore.shared.core.cosmetic.ui.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.shared.core.shop.page.ConfirmationPage;
import com.houzicore.shared.common.CurrencyType;
import com.houzicore.shared.core.cosmetic.CosmeticManager;
import com.houzicore.shared.core.cosmetic.ui.CosmeticShop;
import com.houzicore.shared.core.cosmetic.ui.page.MountPage;
import com.houzicore.shared.core.mount.Mount;

public class MountButton implements IButton {
	private final Mount<?> _mount;
	private final MountPage _page;

	public MountButton(Mount<?> mount, MountPage page) {
		_mount = mount;
		_page = page;
	}

	@Override
	public void onClick(final Player player, ClickType clickType) {
		_page.getShop().openPageForPlayer(player, new ConfirmationPage<>(_page.getPlugin(), _page.getShop(),
				_page.getClientManager(), _page.getDonationManager(), new Runnable() {
					@Override
					public void run() {
						_page.getPlugin().getInventoryManager().addItemToInventory(null, player, "Mount",
								_mount.GetName(), 1);
						_page.refresh();
					}
				}, _page, _mount, CurrencyType.Coins, player));
	}
}
