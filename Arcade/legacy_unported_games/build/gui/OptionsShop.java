package com.houzicore.arcade.nautilus.game.arcade.game.games.build.gui;

import org.bukkit.entity.Player;

import com.houzicore.shared.core.account.CoreClientManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.games.build.Build;
import com.houzicore.arcade.nautilus.game.arcade.game.games.build.gui.page.OptionsPage;

public class OptionsShop extends ShopBase<ArcadeManager>
{
	private Build _game;

	public OptionsShop(Build game, ArcadeManager plugin, CoreClientManager clientManager, DonationManager donationManager)
	{
		super(plugin, clientManager, donationManager, "Options");
		_game = game;
	}

	@Override
	protected ShopPageBase<ArcadeManager, ? extends ShopBase<ArcadeManager>> buildPagesFor(Player player)
	{
		return new OptionsPage(_game, getPlugin(), this, getClientManager(), getDonationManager(), player);
	}
}
