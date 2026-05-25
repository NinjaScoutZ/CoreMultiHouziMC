package com.houzicore.shared.core.cosmetic.ui.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.shared.core.mount.Mount;

public class DeactivateMountButton implements IButton {
	private final Mount<?> _mount;
	private final ShopPageBase<?, ?> _page;

	public DeactivateMountButton(Mount<?> mount, ShopPageBase<?, ?> page) {
		_mount = mount;
		_page = page;
	}

	@Override
	public void onClick(Player player, ClickType clickType) {
		_page.playAcceptSound(player);
		_mount.Disable(player);
		_page.refresh();
	}
}
