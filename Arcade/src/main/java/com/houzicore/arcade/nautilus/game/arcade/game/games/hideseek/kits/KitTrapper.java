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

public class KitTrapper extends Kit
{
    public KitTrapper(ArcadeManager manager)
    {
        super(manager, "Trapper", KitAvailability.Gem, 6000,
                // EN
                new String[] 
                {
                        com.houzicore.shared.common.util.C.cGray + "Zone-control Hunter that wins by denying escape routes.",
                        com.houzicore.shared.common.util.C.cGray + "Thrown web fields and bow pressure turn choke points into easy follow-up kills.",
                        "",
                        com.houzicore.shared.common.util.C.cGreen + "Pros: " + com.houzicore.shared.common.util.C.cWhite + "Excellent catch potential and chase control",
                        com.houzicore.shared.common.util.C.cRed + "Cons: " + com.houzicore.shared.common.util.C.cWhite + "Light armor in open fights"
                }, 
                // TH
                new String[] 
                {
                        com.houzicore.shared.common.util.C.cGray + "นักล่าสายคุมเส้นหนีที่ชนะด้วยการปิดทางมากกว่า reveal ตรง",
                        com.houzicore.shared.common.util.C.cGray + "ใยแมงมุมแบบโยนกระจายและธนูทำให้ choke point กลายเป็นจุดตาย",
                        "",
                        com.houzicore.shared.common.util.C.cGreen + "Pros: " + com.houzicore.shared.common.util.C.cWhite + "ดักจับเก่งมากและไล่ซ้ำหลังติดใยได้ไว",
                        com.houzicore.shared.common.util.C.cRed + "Cons: " + com.houzicore.shared.common.util.C.cWhite + "ถ้าเปิดไฟต์ในที่โล่งจะบางกว่าสายอื่น"
                }, 
                new Perk[] 
                {
                        new PerkDummy("Speed II", new String[] {
                                "Permanent Speed II helps you throw web fields first and chase trapped Hiders.",
                                "Use your speed to lock choke points before the Hider can route around them."
                        })
                }, 
                EntityType.ZOMBIE,
                new ItemStack(Material.LEATHER_BOOTS));

        setLanguageKey("trapper");
        for (Perk perk : GetPerks())
        {
            perk.setLanguageKey("speed_ii");
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
