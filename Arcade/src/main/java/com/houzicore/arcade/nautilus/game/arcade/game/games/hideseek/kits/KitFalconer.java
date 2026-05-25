package com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.kits;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.traits.TraitSkySweep;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KitFalconer extends Kit
{
    public KitFalconer(ArcadeManager manager)
    {
        super(manager, "Falconer", KitAvailability.Gem, 7000,
                new String[]
                {
                        com.houzicore.shared.common.util.C.cGray + "Aerial scout that keeps a hawk circling overhead for a full scan window.",
                        com.houzicore.shared.common.util.C.cGray + "Sky Sweep leaves faint tracks toward nearby Hiders and feeds your chase speed.",
                        "",
                        com.houzicore.shared.common.util.C.cGreen + "Pros: " + com.houzicore.shared.common.util.C.cWhite + "Sharp mover detection and chase follow-up",
                        com.houzicore.shared.common.util.C.cRed + "Cons: " + com.houzicore.shared.common.util.C.cWhite + "Falls off if prey stays still or teammates do not collapse"
                },
                new String[]
                {
                        com.houzicore.shared.common.util.C.cGray + "นักล่าสายสอดแนมทางอากาศที่ปล่อยเหยี่ยวบินวนเหนือหัวต่อเนื่อง",
                        com.houzicore.shared.common.util.C.cGray + "Sky Sweep จะลากรอยจาง ๆ ไปหา Hider ใกล้ตัวและช่วยเติมสปีดตอนปิดงาน",
                        "",
                        com.houzicore.shared.common.util.C.cGreen + "Pros: " + com.houzicore.shared.common.util.C.cWhite + "เปิดไล่เป้าหมายที่ขยับหนีได้คมมาก",
                        com.houzicore.shared.common.util.C.cRed + "Cons: " + com.houzicore.shared.common.util.C.cWhite + "ถ้าเหยื่อไม่ขยับหรือทีมไม่ตามซ้ำ มูลค่าจะตก"
                },
                new Perk[]
                {
                        new TraitSkySweep()
                },
                EntityType.PARROT,
                new ItemStack(Material.FEATHER));

        setLanguageKey("falconer");
        for (Perk perk : GetPerks())
        {
            perk.setLanguageKey("sky_sweep");
        }
    
        // Configurator Pattern Override
        com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.KitConfigData fallback = new com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.KitConfigData();
        fallback.name = this._kitName;
        fallback.availability = this._kitAvailability;
        fallback.cost = this._cost;
        fallback.entityType = this._entityType;
        fallback.displayItem = this._displayItem;
        fallback.descEn = this._kitDesc;
        fallback.descTh = this._kitDescTh;
        
        com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.KitConfigData data = com.houzicore.arcade.nautilus.game.arcade.kit.KitConfigLoader.loadKitData(manager.getPlugin(), "proprush-kits.yml", this._kitName.toLowerCase().replace(" ", "_"), fallback);
        
        this._kitName = data.name;
        this._kitAvailability = data.availability;
        this._cost = data.cost;
        this._entityType = data.entityType;
        if (data.displayItem != null) {
            this._displayItem = data.displayItem;
            this._itemInHand = new org.bukkit.inventory.ItemStack(data.displayItem);
        }
        this._kitDesc = data.descEn;
        this._kitDescTh = data.descTh;
}

    @Override
    public void GiveItems(Player player)
    {
    }
}
