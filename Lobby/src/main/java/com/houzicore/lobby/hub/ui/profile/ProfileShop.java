package com.houzicore.lobby.hub.ui.profile;

import org.bukkit.entity.Player;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.account.CoreClient;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.lobby.hub.HubManager;

public class ProfileShop extends ShopBase<HubManager> 
{
	public ProfileShop(HubManager plugin, CoreClientManager clientManager, DonationManager donationManager) 
	{
		super(plugin, clientManager, donationManager, "My Profile");
	}

	@Override
	protected ShopPageBase<HubManager, ? extends ShopBase<HubManager>> buildPagesFor(Player player) 
	{
		return new ProfilePage(getPlugin(), this, getClientManager(), getDonationManager(), player);
	}
}
