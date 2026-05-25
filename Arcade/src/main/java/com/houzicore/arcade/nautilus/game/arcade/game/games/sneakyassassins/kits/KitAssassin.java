package com.houzicore.arcade.nautilus.game.arcade.game.games.sneakyassassins.kits;

import com.houzicore.arcade.*;
import com.houzicore.arcade.nautilus.game.arcade.kit.*;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.*;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;

public class KitAssassin extends SneakyAssassinKit
{
	public KitAssassin(ArcadeManager manager, EntityType disguiseType)
	{
		super(manager, "Ranged Assassin", KitAvailability.Gem, 
				new String[]
						{
				"Skilled at ranged assassination!"
						}, 
						new Perk[]
								{
				new PerkSmokebomb(Material.INK_SAC, 3, true)
								}, 
								new ItemStack(Material.BOW),
				disguiseType);
	}

	@Override
	public void GiveItems(Player player)
	{
		super.GiveItems(player);

		player.getInventory().addItem(new ItemStack(Material.BOW));
		player.getInventory().addItem(new ItemStack(Material.ARROW, 32));
	}
}
