package com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.gui;

import org.bukkit.entity.Player;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.SpeedBuilders;
import com.houzicore.arcade.nautilus.game.arcade.game.games.speedbuilders.lang.SpeedBuildersLang;

public class SpectatorVoteShop extends ShopBase<ArcadeManager>
{
    private final SpeedBuilders _game;

    public SpectatorVoteShop(SpeedBuilders game, ArcadeManager manager, CoreClientManager clientManager, DonationManager donationManager)
    {
        super(manager, clientManager, donationManager, "Vote for Creativity");
        _game = game;
    }

    public SpeedBuilders getGame()
    {
        return _game;
    }

    @Override
    protected ShopPageBase<ArcadeManager, ? extends ShopBase<ArcadeManager>> buildPagesFor(Player player)
    {
        String title = SpeedBuildersLang.get().get(player, "speedbuilders.gui.vote.title");
        return new SpectatorVoteShopPage(getGame(), getPlugin(), this, getClientManager(), getDonationManager(), title, player);
    }
}
