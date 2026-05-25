package com.houzicore.arcade.nautilus.game.arcade.game.games.searchanddestroy.kits.trooper;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkBullsCharge;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkChampion;

public class KitChampion extends Kit
{
    public KitChampion(ArcadeManager manager)
    {
        super(manager, "Champion", KitAvailability.Hide, // EN
				new String[]
            {
                "Grow stronger with every kill"
            }, 
				// TH
				new String[]
            {
                "[TH] Grow stronger with every kill"
            }, 
				new Perk[]
            {
                    new PerkBullsCharge(), new PerkChampion()
            }, EntityType.ZOMBIE, new ItemStack(Material.IRON_SWORD));
    }

    @Override
    public void GiveItems(Player player)
    {
        PlayerInventory inv = player.getInventory();
        inv.setItem(2, new ItemBuilder(Material.BLAZE_POWDER).setTitle(ChatColor.GOLD + "Fuse").build());
        inv.addItem(new ItemBuilder(Material.IRON_AXE)
                .setTitle(C.cGreen + "Right-Click" + C.cWhite + " - " + C.cYellow + "Bulls Charge").setUnbreakable(true).build());
        inv.addItem(new ItemStack(Material.GOLDEN_APPLE, 2));

        inv.setArmorContents(new ItemStack[]
            {
                    new ItemStack(Material.IRON_BOOTS), new ItemStack(Material.IRON_LEGGINGS),
                    new ItemStack(Material.IRON_CHESTPLATE), new ItemStack(Material.IRON_HELMET)
            });

        inv.setItem(8,
                new ItemBuilder(Material.LEATHER_CHESTPLATE).setColor(this.Manager.GetGame().GetTeam(player).GetColorBase())
                        .setTitle(ChatColor.WHITE + "Evolve Kit Menu").addLore("Right click to use").build());
    }

}
