package com.houzicore.arcade.nautilus.game.arcade.game.games.searchanddestroy.kits.bow;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.games.castlesiege.kits.KitHuman;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkBarrage;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkQuickshot;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class KitThunderstorm extends KitHuman
{
    public KitThunderstorm(ArcadeManager manager)
    {
        super(manager, "Thunderstorm", KitAvailability.Hide, // EN
				new String[]
            {
                "Chargeup your bow to release a barrage of arrows." + "The longer you charge your bow"
                        + "The more arrows are added to the barrage."
            }, 
				// TH
				new String[]
            {
                "[TH] Chargeup your bow to release a barrage of arrows." + "[TH] The longer you charge your bow"
                        + "[TH] The more arrows are added to the barrage."
            }, 
				new Perk[]
            {
                    new PerkQuickshot("Quickshot", 2, 8000), new PerkBarrage(10, 400, false, false, true)
            }, EntityType.SKELETON, new ItemStack(Material.ARROW, 16));
    }

    @Override
    public void GiveItems(Player player)
    {
        PlayerInventory inv = player.getInventory();
        inv.setItem(2, new ItemBuilder(Material.BLAZE_POWDER).setTitle(ChatColor.GOLD + "Fuse").build());
        inv.addItem(new ItemBuilder(Material.IRON_SWORD).setUnbreakable(true).build());
        inv.addItem(new ItemBuilder(Material.BOW).setTitle(C.cGreen + "Left-Click" + C.cWhite + " - " + C.cYellow + "Quickshot")
                .setUnbreakable(true).addEnchantment(Enchantment.INFINITY, 1).build());
        inv.setArmorContents(new ItemStack[]
            {
                    new ItemStack(Material.CHAINMAIL_BOOTS), new ItemStack(Material.CHAINMAIL_LEGGINGS),
                    new ItemStack(Material.CHAINMAIL_CHESTPLATE), new ItemStack(Material.CHAINMAIL_HELMET)
            });
        inv.addItem(new ItemBuilder(Material.BOW).setUnbreakable(true).addEnchantment(Enchantment.INFINITY, 1).build());
        inv.setItem(8,
                new ItemBuilder(Material.LEATHER_CHESTPLATE).setColor(this.Manager.GetGame().GetTeam(player).GetColorBase())
                        .setTitle(ChatColor.WHITE + "Evolve Kit Menu").addLore("Right click to use").build());
        inv.setItem(9, new ItemStack(Material.ARROW, 1));
    }

}
