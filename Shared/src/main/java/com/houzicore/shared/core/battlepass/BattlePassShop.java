package com.houzicore.shared.core.battlepass;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;

public class BattlePassShop extends ShopBase<BattlePassManager> {

	public BattlePassShop(BattlePassManager plugin, CoreClientManager clientManager, DonationManager donationManager, String name) {
		super(plugin, clientManager, donationManager, name);
	}

	@Override
	protected ShopPageBase<BattlePassManager, ? extends ShopBase<BattlePassManager>> buildPagesFor(Player player) {
		return new BattlePassPage(getPlugin(), this, getClientManager(), getDonationManager(), player);
	}
}
