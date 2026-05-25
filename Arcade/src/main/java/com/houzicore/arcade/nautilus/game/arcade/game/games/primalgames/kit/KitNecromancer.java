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

public class KitNecromancer extends Kit
{
	public KitNecromancer(ArcadeManager manager)
	{
		super(manager, "Necromancer", KitAvailability.Gem, 5000,

				new String[] 
						{
				"Cool undead guy and stuff"
						}, 

						new Perk[] 
								{
					new PerkSkeletons(true)
								}, 
								EntityType.ZOMBIE,
								new ItemStack(Material.PLAYER_HEAD));

	}

	@Override
	public void GiveItems(Player player)
	{
		
	}
}
