package com.houzicore.shared.core.preferences.ui;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.preferences.PreferencesManager;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.shared.core.lang.LangManager;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PreferencesShop extends ShopBase<PreferencesManager> {
	public PreferencesShop(PreferencesManager plugin, CoreClientManager clientManager,
			com.houzicore.shared.core.donation.DonationManager donationManager) {
		super(plugin, clientManager, donationManager, "User Preferences");
	}

	@Override
	protected ShopPageBase<PreferencesManager, ? extends ShopBase<PreferencesManager>> buildPagesFor(Player player) {
		return new PreferencesPage(getPlugin(), this, getClientManager(), getDonationManager(),
				"          " + ChatColor.UNDERLINE + LangManager.get().get(player, "prefs.title"), player);
	}
}
