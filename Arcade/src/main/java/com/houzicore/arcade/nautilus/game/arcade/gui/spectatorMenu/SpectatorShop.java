package com.houzicore.arcade.nautilus.game.arcade.gui.spectatorMenu;

import org.bukkit.entity.Player;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.CurrencyType;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.addons.CompassAddon;
import com.houzicore.arcade.nautilus.game.arcade.gui.spectatorMenu.page.SpectatorPage;

/**
 * Created by shaun on 14-09-24.
 */
public class SpectatorShop extends ShopBase<CompassAddon>
{

	private ArcadeManager _arcadeManager;

	public SpectatorShop(CompassAddon plugin, ArcadeManager arcadeManager, CoreClientManager clientManager, DonationManager donationManager, CurrencyType... currencyTypes)
	{
		super(plugin, clientManager, donationManager, "Spectate Menu", currencyTypes);
		_arcadeManager = arcadeManager;
	}

	@Override
	protected ShopPageBase<CompassAddon, ? extends ShopBase<CompassAddon>> buildPagesFor(Player player)
	{
		return new SpectatorPage(getPlugin(), _arcadeManager, this, getClientManager(), getDonationManager(), player);
	}

	public void update()
	{
		for (ShopPageBase<CompassAddon, ? extends ShopBase<CompassAddon>> shopPage : getPlayerPageMap().values())
		{
			shopPage.refresh();
		}
	}

}
