package com.houzicore.arcade.nautilus.game.arcade.game.games.searchanddestroy.kits.bow;

import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.games.castlesiege.kits.KitHuman;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkPinned;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class KitPinner extends KitHuman
{

    public KitPinner(ArcadeManager manager)
    {
        super(manager, "Pinner", KitAvailability.Hide, // EN
				new String[]
            {
                    "Your arrows enchanted with gravity magic.", "Victims find it hard to lift", "Their bodies are sluggish"
            }, 
				// TH
				new String[]
            {
                    "[TH] Your arrows enchanted with gravity magic.", "[TH] Victims find it hard to lift", "[TH] Their bodies are sluggish"
            }, 
				new Perk[]
            {
                new PerkPinned()
            }, EntityType.SKELETON, new ItemStack(Material.OBSIDIAN));
    }

    @Override
    public void GiveItems(Player player)
    {
        PlayerInventory inv = player.getInventory();
        inv.setArmorContents(new ItemStack[]
            {
                    new ItemStack(Material.CHAINMAIL_BOOTS), new ItemStack(Material.CHAINMAIL_LEGGINGS),
                    new ItemStack(Material.CHAINMAIL_CHESTPLATE), new ItemStack(Material.CHAINMAIL_HELMET)
            });
        inv.setItem(2, new ItemBuilder(Material.BLAZE_POWDER).setTitle(ChatColor.GOLD + "Fuse").build());
        inv.addItem(new ItemBuilder(Material.IRON_SWORD).setUnbreakable(true).build());
        inv.addItem(new ItemBuilder(Material.BOW).setUnbreakable(true).addEnchantment(Enchantment.INFINITY, 1).build());
        inv.setItem(8,
                new ItemBuilder(Material.LEATHER_CHESTPLATE).setColor(this.Manager.GetGame().GetTeam(player).GetColorBase())
                        .setTitle(ChatColor.WHITE + "Evolve Kit Menu").addLore("Right click to use").build());
        inv.setItem(9, new ItemStack(Material.ARROW, 1));
    }

}
