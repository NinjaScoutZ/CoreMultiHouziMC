package com.houzicore.arcade.nautilus.game.arcade.managers.voting;

import org.bukkit.entity.Player;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.managers.MapVotingManager;

public class MapVotingShop extends ShopBase<ArcadeManager> {

    private final MapVotingManager _votingManager;

    public MapVotingShop(ArcadeManager plugin, CoreClientManager clientManager, DonationManager donationManager, MapVotingManager votingManager) {
        super(plugin, clientManager, donationManager, "Poll: What Theme?");
        _votingManager = votingManager;
    }

    @Override
    protected ShopPageBase<ArcadeManager, ? extends ShopBase<ArcadeManager>> buildPagesFor(Player player) {
        return new MapVotingPage(getPlugin(), this, getClientManager(), getDonationManager(), "Poll: What Theme?", player, _votingManager);
    }
}
