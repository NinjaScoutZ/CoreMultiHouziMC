package com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.kits;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkDummy;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KitDestroyer extends Kit
{
    public KitDestroyer(ArcadeManager manager)
    {
        super(manager, "Destroyer", KitAvailability.Gem, 4000,
                // EN
                new String[] 
                {
                        com.houzicore.shared.common.util.C.cGray + "Area-pressure Hunter that checks suspicious rooms with force.",
                        com.houzicore.shared.common.util.C.cGray + "Flare reveals clustered Hiders before you step into the room.",
                        "",
                        com.houzicore.shared.common.util.C.cGreen + "Pros: " + com.houzicore.shared.common.util.C.cWhite + "Strong room breach and reveal pressure",
                        com.houzicore.shared.common.util.C.cRed + "Cons: " + com.houzicore.shared.common.util.C.cWhite + "Slower rotations between fights"
                }, 
                // TH
                new String[]
                {
                        com.houzicore.shared.common.util.C.cGray + "นักล่าคุมห้องที่ใช้แรงกดดันเปิดจุดซ่อนต้องสงสัย",
                        com.houzicore.shared.common.util.C.cGray + "Flare ช่วย reveal เป้าหมายก่อนเดินเข้าเคลียร์พื้นที่",
                        "",
                        com.houzicore.shared.common.util.C.cGreen + "Pros: " + com.houzicore.shared.common.util.C.cWhite + "เก่งมากกับห้องแน่นและจุดซ่อนที่อ่านยาก",
                        com.houzicore.shared.common.util.C.cRed + "Cons: " + com.houzicore.shared.common.util.C.cWhite + "ย้ายจุดช้ากว่าสาย scout"
                }, 
                new Perk[] 
                {
                        new PerkDummy("Flare", new String[] {
                                "Right-click Fire Charge to throw a revealing flare.",
                                "Use it to force hidden props out before committing to a room check."
                        })
                }, 
                EntityType.WITHER_SKELETON,
                new ItemStack(Material.FIRE_CHARGE));

        setLanguageKey("destroyer");
        for (Perk perk : GetPerks())
        {
            perk.setLanguageKey("flare");
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
