package com.houzicore.arcade.nautilus.game.arcade.game.games.build.gui;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.houzicore.shared.core.account.CoreClientManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.games.build.BuildData;
import com.houzicore.arcade.nautilus.game.arcade.game.games.build.gui.page.MobPage;

public class MobShop extends ShopBase<ArcadeManager>
{
	public MobShop(ArcadeManager plugin, CoreClientManager clientManager, DonationManager donationManager)
	{
		super(plugin, clientManager, donationManager, "Mob Options");
	}

	protected ShopPageBase<ArcadeManager, ? extends ShopBase<ArcadeManager>> buildPagesFor(Player player, BuildData data, Entity entity)
	{
		return new MobPage(getPlugin(), this, getClientManager(), getDonationManager(), player, data, entity);
	}

	public boolean attemptShopOpen(Player player, BuildData data, Entity entity)
	{
		if (!getOpenedShop().contains(player.getUniqueId()))
		{
			if (!canOpenShop(player))
				return false;

			getOpenedShop().add(player.getUniqueId());

			openShopForPlayer(player);
			if (!getPlayerPageMap().containsKey(player.getUniqueId()))
			{
				getPlayerPageMap().put(player.getUniqueId(), buildPagesFor(player, data, entity));
			}

			openPageForPlayer(player, getOpeningPageForPlayer(player));

			return true;
		}

		return false;
	}

	@Override
	protected ShopPageBase<ArcadeManager, ? extends ShopBase<ArcadeManager>> buildPagesFor(Player player)
	{
		return null;
	}
}
