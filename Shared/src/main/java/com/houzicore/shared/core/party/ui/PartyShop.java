package com.houzicore.shared.core.party.ui;

import org.bukkit.entity.Player;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.party.PartyManager;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;

public class PartyShop extends ShopBase<PartyManager> {
	public PartyShop(PartyManager plugin, CoreClientManager clientManager) {
		super(plugin, clientManager, null, "Party");
	}

	@Override
	protected ShopPageBase<PartyManager, ? extends ShopBase<PartyManager>> buildPagesFor(Player player) {
		return new PartyPage(getPlugin(), this, getClientManager(), null, player);
	}
}
