package com.houzicore.arcade.nautilus.game.arcade.game.games.sneakyassassins.kits;

import com.houzicore.shared.core.achievement.Achievement;
import com.houzicore.shared.common.util.*;
import com.houzicore.shared.core.itemstack.*;
import com.houzicore.arcade.*;
import com.houzicore.arcade.nautilus.game.arcade.kit.*;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.*;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;

public class KitBriber extends SneakyAssassinKit
{
	public KitBriber(ArcadeManager manager, EntityType disguiseType)
	{
		super(manager, "Briber", KitAvailability.Achievement, 
				new String[]
						{
				"Pay Villagers to attack other players!"
						}, 
						new Perk[]
								{
				new PerkSmokebomb(Material.INK_SAC, 3, true),
								}, 
								new ItemStack(Material.EMERALD),
				disguiseType);
		
		this.setAchievementRequirements(new Achievement[] 
				{
				Achievement.SNEAK_ASSASSINS_I_SEE_YOU,
				Achievement.SNEAK_ASSASSINS_INCOMPETENCE,
				Achievement.SNEAK_ASSASSINS_MASTER_ASSASSIN,
				Achievement.SNEAK_ASSASSINS_THE_MASTERS_MASTER,
				Achievement.SNEAKY_ASSASSINS_WINS,
				});
	}

	@Override
	public void GiveItems(Player player)
	{
		super.GiveItems(player);
 
		player.getInventory().addItem(ItemStackFactory.Instance.CreateStack(Material.EMERALD, (byte) 0, 4,
				C.cYellow + C.Bold + "Right-Click Villager" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Bribe Villager",
				new String[]
						{
								ChatColor.RESET + "Pay a villager to help you.",
								ChatColor.RESET + "It will attack the nearest",
								ChatColor.RESET + "enemy for 15 seconds.",

						}));
	}
}
