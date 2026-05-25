package com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.kit;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.*;

public class KitLooter extends Kit
{
	public KitLooter(ArcadeManager manager)
	{
		super(manager, "Looter", KitAvailability.Free, 

				new String[] 
						{
				"Defeat your opponents with your swag loots!"
						}, 

						new Perk[] 
								{
				new PerkLooter(),
								}, 
								EntityType.ZOMBIE,
								new ItemStack(Material.CHEST));

	}

	@Override
	public void GiveItems(Player player) 
	{
		
	}
}
