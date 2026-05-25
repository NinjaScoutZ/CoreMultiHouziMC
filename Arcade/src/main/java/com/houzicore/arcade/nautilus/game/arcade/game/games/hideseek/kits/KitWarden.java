package com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.kits;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.game.games.hideseek.traits.TraitEchoSentry;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KitWarden extends Kit
{
    public KitWarden(ArcadeManager manager)
    {
        super(manager, "Warden", KitAvailability.Gem, 8000,
                new String[]
                {
                        com.houzicore.shared.common.util.C.cGray + "Lane-control Hunter that punishes movers crossing key routes.",
                        com.houzicore.shared.common.util.C.cGray + "Echo Sentry locks one crossover and slows anyone trying to slip through.",
                        "",
                        com.houzicore.shared.common.util.C.cGreen + "Pros: " + com.houzicore.shared.common.util.C.cWhite + "Locks rotations and punishes forced crossings",
                        com.houzicore.shared.common.util.C.cRed + "Cons: " + com.houzicore.shared.common.util.C.cWhite + "Needs accurate lane reads to stay valuable"
                },
                new String[]
                {
                        com.houzicore.shared.common.util.C.cGray + "นักล่าสายคุมเลนที่ลงโทษคนขยับผ่านจุดสำคัญ",
                        com.houzicore.shared.common.util.C.cGray + "ตั้ง Echo Sentry ไว้ล็อกจุดตัดและทำให้เป้าที่ฝืนผ่านช้าลง",
                        "",
                        com.houzicore.shared.common.util.C.cGreen + "Pros: " + com.houzicore.shared.common.util.C.cWhite + "ล็อก rotation และบีบเส้นทางหนีได้ชัด",
                        com.houzicore.shared.common.util.C.cRed + "Cons: " + com.houzicore.shared.common.util.C.cWhite + "ต้องอ่านเส้นทางให้ถูก ไม่งั้นค่า sentry จะหาย"
                },
                new Perk[]
                {
                        new TraitEchoSentry()
                },
                EntityType.WARDEN,
                new ItemStack(Material.SCULK_SENSOR));

        setLanguageKey("warden");
        for (Perk perk : GetPerks())
        {
            perk.setLanguageKey("echo_sentry");
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
