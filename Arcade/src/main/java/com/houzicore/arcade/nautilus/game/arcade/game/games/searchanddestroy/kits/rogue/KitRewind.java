package com.houzicore.arcade.nautilus.game.arcade.game.games.searchanddestroy.kits.rogue;

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
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkEvade;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkRewind;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkShadowmeld;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkSpeed;

public class KitRewind extends Kit
{

    public KitRewind(ArcadeManager manager)
    {
        super(manager, "Rewind", KitAvailability.Hide, // EN
				new String[]
            {

            }, 
				// TH
				new String[]
            {

            }, 
				new Perk[]
            {
                    new PerkSpeed(1), new PerkShadowmeld(), new PerkEvade(), new PerkRewind()

            }, EntityType.ZOMBIE, new ItemStack(Material.DIAMOND_SWORD));
    }

    @Override
    public void GiveItems(Player player)
    {
        PlayerInventory inv = player.getInventory();
        inv.setItem(2, new ItemBuilder(Material.BLAZE_POWDER).setTitle(ChatColor.GOLD + "Fuse").build());
        inv.addItem(new ItemBuilder(Material.DIAMOND_SWORD)
                .setTitle(C.cGreen + "Hold Block" + C.cWhite + " - " + C.cYellow + "Evade").setUnbreakable(true).build());
        inv.addItem(new ItemBuilder(Material.NETHER_STAR)
                .setTitle(C.cGreen + "Right-Click" + C.cWhite + " - " + C.cYellow + "Rewind").setUnbreakable(true).build());
        inv.setItem(8,
                new ItemBuilder(Material.LEATHER_CHESTPLATE).setColor(this.Manager.GetGame().GetTeam(player).GetColorBase())
                        .setTitle(ChatColor.WHITE + "Evolve Kit Menu").addLore("Right click to use").build());
        inv.setArmorContents(new ItemStack[]
            {
                    new ItemStack(Material.LEATHER_BOOTS), new ItemStack(Material.LEATHER_LEGGINGS),
                    new ItemStack(Material.LEATHER_CHESTPLATE), new ItemStack(Material.LEATHER_HELMET)
            });
    }
}
