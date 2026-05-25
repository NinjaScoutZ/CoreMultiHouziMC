package com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.kits;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.traits.TraitSmokeBomb;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KitSaboteur extends Kit
{
    public KitSaboteur(ArcadeManager manager)
    {
        super(manager, "Saboteur", KitAvailability.Gem, 6000,
                // EN
                new String[] 
                {
                        com.houzicore.shared.common.util.C.cGray + "Disruption Hunter that starts fights on your terms.",
                        com.houzicore.shared.common.util.C.cGray + "Smoke Bomb blinds and slows Hiders for a clean collapse.",
                        "",
                        com.houzicore.shared.common.util.C.cGreen + "Pros: " + com.houzicore.shared.common.util.C.cWhite + "Strong opener and anti-hide utility",
                        com.houzicore.shared.common.util.C.cRed + "Cons: " + com.houzicore.shared.common.util.C.cWhite + "Weak solo cleanup if the engage misses"
                }, 
                // TH
                new String[]
                {
                        com.houzicore.shared.common.util.C.cGray + "นักล่าสาย disruption ที่เปิดไฟต์ให้ทีมได้ก่อน",
                        com.houzicore.shared.common.util.C.cGray + "Smoke Bomb ทำให้ Hider มองไม่เห็นและหนีช้าลงพร้อมกัน",
                        "",
                        com.houzicore.shared.common.util.C.cGreen + "Pros: " + com.houzicore.shared.common.util.C.cWhite + "เก่งมากในการเปิดจังหวะบุกและตัดการมองเห็น",
                        com.houzicore.shared.common.util.C.cRed + "Cons: " + com.houzicore.shared.common.util.C.cWhite + "ถ้าเข้าไม่ถึงหลังใช้สกิลจะเสีย value เยอะ"
                }, 
                new Perk[] 
                {
                        new TraitSmokeBomb()
                }, 
                EntityType.CREEPER,
                new ItemStack(Material.GUNPOWDER));

        setLanguageKey("saboteur");
        for (Perk perk : GetPerks())
        {
            perk.setLanguageKey("smoke_bomb");
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
