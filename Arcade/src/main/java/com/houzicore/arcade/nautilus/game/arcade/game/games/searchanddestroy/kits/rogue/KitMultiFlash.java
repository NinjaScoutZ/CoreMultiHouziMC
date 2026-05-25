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
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkFlash;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkShadowmeld;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkSpeed;

public class KitMultiFlash extends Kit
{
    public KitMultiFlash(ArcadeManager manager)
    {
        super(manager, "Multi-Flash", KitAvailability.Hide, // EN
				new String[]
            {

            }, 
				// TH
				new String[]
            {

            }, 
				new Perk[]
            {
                    new PerkSpeed(1), new PerkShadowmeld(), new PerkFlash(4, false)
            }, EntityType.ZOMBIE, new ItemStack(Material.DIAMOND_AXE));
    }

    @Override
    public void GiveItems(Player player)
    {
        PlayerInventory inv = player.getInventory();
        inv.setItem(2, new ItemBuilder(Material.BLAZE_POWDER).setTitle(ChatColor.GOLD + "Fuse").build());
        inv.addItem(new ItemBuilder(Material.DIAMOND_AXE)
                .setTitle(C.cGreen + "Right-Click" + C.cWhite + " - " + C.cYellow + "Flash").setUnbreakable(true).build());

        inv.setArmorContents(new ItemStack[]
            {
                    new ItemStack(Material.LEATHER_BOOTS), new ItemStack(Material.LEATHER_LEGGINGS),
                    new ItemStack(Material.LEATHER_CHESTPLATE), new ItemStack(Material.LEATHER_HELMET)
            });
        inv.setItem(8,
                new ItemBuilder(Material.LEATHER_CHESTPLATE).setColor(this.Manager.GetGame().GetTeam(player).GetColorBase())
                        .setTitle(ChatColor.WHITE + "Evolve Kit Menu").addLore("Right click to use").build());
    }
}
