package com.houzicore.arcade.nautilus.game.arcade.game.games.searchanddestroy;

import java.util.ArrayList;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.CurrencyType;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.arcade.nautilus.game.arcade.game.games.searchanddestroy.KitManager.UpgradeKit;

import org.bukkit.entity.Player;

public class KitEvolveShop extends ShopBase<KitEvolve>
{

    private SearchAndDestroy _arcadeManager;
    private ArrayList<UpgradeKit> _kits;

    public KitEvolveShop(KitEvolve plugin, SearchAndDestroy arcadeManager, CoreClientManager clientManager,
            DonationManager donationManager, ArrayList<UpgradeKit> kits, CurrencyType... currencyTypes)
    {
        super(plugin, clientManager, donationManager, "Kit Evolve Menu", currencyTypes);
        _arcadeManager = arcadeManager;
        _kits = kits;
    }

    @Override
    protected ShopPageBase<KitEvolve, ? extends ShopBase<KitEvolve>> buildPagesFor(Player player)
    {
        return new KitEvolvePage(getPlugin(), _arcadeManager, this, getClientManager(), getDonationManager(), player, _kits);
    }

    public void update()
    {
        for (ShopPageBase<KitEvolve, ? extends ShopBase<KitEvolve>> shopPage : getPlayerPageMap().values())
        {
            shopPage.refresh();
        }
    }
}
