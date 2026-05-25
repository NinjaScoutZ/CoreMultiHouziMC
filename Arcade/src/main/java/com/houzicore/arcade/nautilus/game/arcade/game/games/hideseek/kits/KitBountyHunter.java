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

public class KitBountyHunter extends Kit
{
    public KitBountyHunter(ArcadeManager manager)
    {
        super(manager, "Bounty Hunter", KitAvailability.Gem, 8000,
                // EN
                new String[] 
                {
                        com.houzicore.shared.common.util.C.cGray + "Bruiser Hunter that finishes revealed targets with raw pressure.",
                        com.houzicore.shared.common.util.C.cGray + "Heavy armor buys time until Bounty Dash closes the last gap.",
                        "",
                        com.houzicore.shared.common.util.C.cGreen + "Pros: " + com.houzicore.shared.common.util.C.cWhite + "High cleanup power and strong armor",
                        com.houzicore.shared.common.util.C.cRed + "Cons: " + com.houzicore.shared.common.util.C.cWhite + "Very slow without information or setup"
                }, 
                // TH
                new String[]
                {
                        com.houzicore.shared.common.util.C.cGray + "นักล่าสายปิดงานที่แลกความเร็วกับพลังบีบและเกราะหนา",
                        com.houzicore.shared.common.util.C.cGray + "เด่นสุดเมื่อทีมเปิดข้อมูลให้แล้วและต้องการคนเข้าเก็บ kill",
                        "",
                        com.houzicore.shared.common.util.C.cGreen + "Pros: " + com.houzicore.shared.common.util.C.cWhite + "ปิดเป้าหมาย revealed ได้แรงและยืนไฟต์ได้นาน",
                        com.houzicore.shared.common.util.C.cRed + "Cons: " + com.houzicore.shared.common.util.C.cWhite + "ถ้าไม่มีคนเปิดทางให้จะตามเกมยากมาก"
                }, 
                new Perk[] 
                {
                        new PerkDummy("Bounty Dash", new String[] {
                                "Right-click Crossbow to launch a heavy gap-closing dash.",
                                "Best used after a reveal, when the target has little space left to escape."
                        })
                }, 
                EntityType.PILLAGER,
                new ItemStack(Material.CROSSBOW));

        setLanguageKey("bounty_hunter");
        for (Perk perk : GetPerks())
        {
            perk.setLanguageKey("bounty_dash");
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
