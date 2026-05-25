package com.houzicore.lobby.hub.modules.nonstop.shop;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.lobby.hub.modules.nonstop.NonstopParkourManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ParkourPage extends ShopPageBase<NonstopParkourManager, ParkourShop> {
    public ParkourPage(NonstopParkourManager plugin, ParkourShop shop, CoreClientManager clientManager,
                      DonationManager donationManager, Player player) {
        super(plugin, shop, clientManager, donationManager, "Parkour Menu", player, 27);
        buildPage();
    }

    @Override
    protected void buildPage() {
        ItemStack glass = ItemStackFactory.Instance.CreateStack(
            Material.LIGHT_BLUE_STAINED_GLASS_PANE, (byte) 0, 1,
            "§bEssence: " + getDonationManager().Get(getPlayer().getName()).GetEssence()
        );
        for (int i = 0; i < getSize(); i++) {
            getInventory().setItem(i, glass);
        }

        boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(getPlayer());

        // Practice Mode (No Timer)
        addButton(11, ItemStackFactory.Instance.CreateStack(
            Material.GOLDEN_BOOTS, (byte) 0, 1, isThai ? "§a§lโหมดฝึกซ้อม (Practice)" : "§a§lPractice Mode",
            new String[]{
                isThai ? "§7กระโดดไปตาม Checkpoint" : "§7Jump through checkpoints",
                isThai ? "§7ไม่มีการจับเวลา" : "§7No timer pressure",
                "§8───────────",
                isThai ? "§eคลิกเพื่อเริ่ม!" : "§eClick to start!"
            }),
            (player, clickType) -> {
                playAcceptSound(player);
                player.closeInventory();
                getPlugin().startCourse(player, false);
            }
        );

        // Challenge Mode (Timer)
        addButton(15, ItemStackFactory.Instance.CreateStack(
            Material.CLOCK, (byte) 0, 1, isThai ? "§c§lโหมดท้าทาย (Challenge)" : "§c§lChallenge Mode",
            new String[]{
                isThai ? "§7จับเวลาเพื่อทำสถิติ" : "§7Timer is active",
                isThai ? "§7วิ่งให้เร็วที่สุด!" : "§7Run as fast as you can!",
                "§8───────────",
                isThai ? "§eคลิกเพื่อเริ่ม!" : "§eClick to start!"
            }),
            (player, clickType) -> {
                playAcceptSound(player);
                player.closeInventory();
                getPlugin().startCourse(player, true);
            }
        );
    }
}
