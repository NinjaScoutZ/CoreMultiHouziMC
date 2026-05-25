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

public class KitBombBug extends Kit
{
    public KitBombBug(ArcadeManager manager)
    {
        super(manager, "Bomb Bug", KitAvailability.Gem, 6500,
                new String[]
                {
                        com.houzicore.shared.common.util.C.cGray + "Trap Hider that can actually down Hunters with lethal burst.",
                        com.houzicore.shared.common.util.C.cGray + "Set explosive bait where greedy checks look natural and costly.",
                        "",
                        com.houzicore.shared.common.util.C.cGreen + "Pros: " + com.houzicore.shared.common.util.C.cWhite + "Real punish window against greedy Hunters",
                        com.houzicore.shared.common.util.C.cRed + "Cons: " + com.houzicore.shared.common.util.C.cWhite + "Needs setup time and believable bait"
                },
                new String[]
                {
                        com.houzicore.shared.common.util.C.cGray + "สายกับดักที่สวน Hunter ได้จริงด้วยระเบิดปิดงาน",
                        com.houzicore.shared.common.util.C.cGray + "วางเหยื่อในจุดที่ดูน่าตี แล้วปล่อยให้การเช็กโลภจ่ายเลือดจริง",
                        "",
                        com.houzicore.shared.common.util.C.cGreen + "Pros: " + com.houzicore.shared.common.util.C.cWhite + "แรงพอจะสวน Hunter ที่เช็กโลภ",
                        com.houzicore.shared.common.util.C.cRed + "Cons: " + com.houzicore.shared.common.util.C.cWhite + "ต้องวางจุดให้เนียน ไม่งั้นเสียของฟรี"
                },
                new Perk[]
                {
                        new PerkDummy("Bomb Shell", new String[] {
                                "Right-click Firework Star to plant a heavy explosive decoy trap.",
                                "Best for punishing greedy checks with real damage, blind, slow, and knockback."
                        })
                },
                EntityType.SILVERFISH,
                new ItemStack(Material.FIREWORK_STAR));

        setLanguageKey("bomb_bug");
        for (Perk perk : GetPerks())
        {
            perk.setLanguageKey("bomb_shell");
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
