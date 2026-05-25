package com.houzicore.shared.core.quest;

import org.bukkit.entity.Player;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;

public class QuestShop extends ShopBase<QuestManager> {

	public QuestShop(QuestManager plugin, CoreClientManager clientManager, DonationManager donationManager, String name) {
		super(plugin, clientManager, donationManager, name);
	}

	@Override
	protected ShopPageBase<QuestManager, ? extends ShopBase<QuestManager>> buildPagesFor(Player player) {
		return new QuestPage(getPlugin(), this, getClientManager(), getDonationManager(), player);
	}
}
