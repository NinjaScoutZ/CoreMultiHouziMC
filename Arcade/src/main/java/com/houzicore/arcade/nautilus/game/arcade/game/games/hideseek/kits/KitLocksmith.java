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

public class KitLocksmith extends Kit
{
    public KitLocksmith(ArcadeManager manager)
    {
        super(manager, "Locksmith", KitAvailability.Gem, 6500,
                new String[]
                {
                        com.houzicore.shared.common.util.C.cGray + "Route Hider that escapes through awkward barriers and corners.",
                        com.houzicore.shared.common.util.C.cGray + "Best when the map has narrow choke points or hidden pathing.",
                        "",
                        com.houzicore.shared.common.util.C.cGreen + "Pros: " + com.houzicore.shared.common.util.C.cWhite + "Can break pursuit through route tricks",
                        com.houzicore.shared.common.util.C.cRed + "Cons: " + com.houzicore.shared.common.util.C.cWhite + "Weak if the room offers no usable escape line"
                },
                new String[]
                {
                        com.houzicore.shared.common.util.C.cGray + "สายเปิดเส้นทางลับที่เด่นกับมุมคับแคบและกำแพงหลอกตา",
                        com.houzicore.shared.common.util.C.cGray + "เหมาะกับแมพที่หนีด้วย route play มากกว่าวิ่งตรง",
                        "",
                        com.houzicore.shared.common.util.C.cGreen + "Pros: " + com.houzicore.shared.common.util.C.cWhite + "ตัดเส้นไล่ล่าด้วยทางลัดและการหักมุมได้ดี",
                        com.houzicore.shared.common.util.C.cRed + "Cons: " + com.houzicore.shared.common.util.C.cWhite + "ถ้าแมพไม่มีช่องให้เล่นจะดึง value ได้น้อย"
                },
                new Perk[]
                {
                        new PerkDummy("Secret Passage", new String[] {
                                "Right-click Tripwire Hook to slip through a nearby barrier.",
                                "Use it to cut a chase route and reappear on the safer side of a wall."
                        })
                },
                EntityType.ALLAY,
                new ItemStack(Material.TRIPWIRE_HOOK));

        setLanguageKey("locksmith");
        for (Perk perk : GetPerks())
        {
            perk.setLanguageKey("secret_passage");
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
