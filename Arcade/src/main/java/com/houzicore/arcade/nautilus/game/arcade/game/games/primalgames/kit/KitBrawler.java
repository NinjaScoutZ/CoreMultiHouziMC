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

public class KitBrawler extends Kit
{
	public KitBrawler(ArcadeManager manager)
	{
		super(manager, "Brawler", KitAvailability.Gem, 

				new String[] 
						{
				"Giant and muscular, easily smacks others around."
						}, 

						new Perk[] 
								{
				new PerkMammoth(),
				
				new PerkSeismicSlamHG()
								}, 
								EntityType.ZOMBIE,
								new ItemStack(Material.IRON_SWORD));

	}

	@Override
	public void GiveItems(Player player) 
	{
		
	}
}
