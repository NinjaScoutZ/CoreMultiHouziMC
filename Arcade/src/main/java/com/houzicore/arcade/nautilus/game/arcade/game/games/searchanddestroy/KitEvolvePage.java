package com.houzicore.arcade.nautilus.game.arcade.game.games.searchanddestroy;

import java.util.ArrayList;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.arcade.nautilus.game.arcade.game.games.searchanddestroy.KitManager.UpgradeKit;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import com.houzicore.shared.core.lang.LangManager;
import org.bukkit.inventory.ItemStack;

public class KitEvolvePage extends ShopPageBase<KitEvolve, KitEvolveShop>
{
    private ArrayList<UpgradeKit> _kits;
    private SearchAndDestroy _search;

    public KitEvolvePage(KitEvolve plugin, SearchAndDestroy arcadeManager, KitEvolveShop shop, CoreClientManager clientManager,
            DonationManager donationManager, Player player, ArrayList<UpgradeKit> kits)
    {
        super(plugin, shop, clientManager, donationManager, LangManager.get().isThai(player) ? "วิวัฒนาการคลาส" : "Kit Evolution", player);
        _search = arcadeManager;
        _kits = kits;
        buildPage();
    }

    @Override
    protected void buildPage()
    {
        Kit hisKit = _search.GetKit(getPlayer());
        UpgradeKit kit = null;
        
        for (UpgradeKit k : _kits)
        {
            if (k.kit == hisKit)
            {
                kit = k;
                break;
            }
        }
        
        boolean canEvolve = _search.canEvolve(getPlayer());
        addItem(kit.kitSlot, makeItem(kit.kit));
        
        for (int slot : kit.path)
        {
            addItem(slot, makeItem(new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE, 1, (short) 15).setTitle(" ").build()));
        }
        
        if (kit.daddy != null)
        {
            addItem(kit.daddy.kitSlot, makeItem(kit.daddy.kit));
            
            for (int slot : kit.daddy.path)
            {
                addItem(slot, makeItem(new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE, 1, (short) 15).setTitle(" ").build()));
            }
            
            if (kit.daddy.daddy != null)
            {
                addItem(kit.daddy.daddy.kitSlot, makeItem(kit.daddy.daddy.kit));
            }
        }
        
        if (canEvolve)
        {
            for (UpgradeKit child : kit.children)
            {
                for (int slot : child.path)
                    addItem(slot, makeItem(new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE).setTitle(" ").build()));
                
                addButton(child.kitSlot, makeItem(child.kit), new EvolveButton(_search, child.kit));
            }
        }
        
        for (UpgradeKit k : _kits)
        {
            ItemStack item = getItem(k.kitSlot);
            
            if (item == null || item.getType() == Material.AIR)
            {
                addItem(k.kitSlot, makeItem(new ItemBuilder(Material.COAL).setTitle(C.cRed + (LangManager.get().isThai(getPlayer()) ? "ล็อคอยู่" : "Locked")).build()));
            }
        }
        
        for (int slot = 0; slot < this.getSize(); slot++)
        {
            ItemStack item = getItem(slot);
            
            if (item == null || item.getType() == Material.AIR)
            {
                addItem(slot, makeItem(new ItemBuilder(Material.WHITE_STAINED_GLASS_PANE, 1, (short) 12).setTitle(" ").build()));
            }
        }
    }

    /**
     * Why do I need to call this and additem again?
     */
    private ShopItem makeItem(ItemStack item)
    {
        return new ShopItem(item, "", "", 1, false, false);
    }

    private ShopItem makeItem(Kit kit)
    {
        return new ShopItem(new ItemBuilder(kit.GetItemInHand()).setTitle(kit.GetName()).addLore(kit.GetDesc()).build(),
                kit.GetName(), kit.GetName(), 1, false, false);
    }

}
