package com.houzicore.shared.core.ignore.ui;

import org.bukkit.entity.Player;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.ignore.IgnoreManager;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;

public class IgnoreShop extends ShopBase<IgnoreManager> {
	public IgnoreShop(IgnoreManager plugin, CoreClientManager clientManager) {
		super(plugin, clientManager, null, "Ignore List");
	}

	@Override
	protected ShopPageBase<IgnoreManager, ? extends ShopBase<IgnoreManager>> buildPagesFor(Player player) {
		return new IgnorePage(getPlugin(), this, getClientManager(), player);
	}
}
