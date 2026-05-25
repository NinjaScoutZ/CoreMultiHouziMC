package com.houzicore.shared.core.friend.ui;

import org.bukkit.entity.Player;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.friend.FriendManager;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;

public class FriendShop extends ShopBase<FriendManager> {
	
	public FriendShop(FriendManager plugin, CoreClientManager clientManager) {
		super(plugin, clientManager, null, "Friends");
	}

	@Override
	protected ShopPageBase<FriendManager, ? extends ShopBase<FriendManager>> buildPagesFor(Player player) {
		return new FriendPage(getPlugin(), this, getClientManager(), player);
	}
}
