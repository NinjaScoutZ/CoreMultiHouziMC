package com.houzicore.arcade.nautilus.game.arcade.game.games.cards.kits;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class KitPlayer extends Kit
{
	public KitPlayer(ArcadeManager manager)
	{
		super(manager, "Player", KitAvailability.Free, 

				// EN
				new String[] 
						{
				";dsgoasdyay"
						}, 
				// TH
				new String[] 
						{
				"[TH] ;dsgoasdyay"
						}, 
				new Perk[] 
								{ 
								}, 
								EntityType.SKELETON,
								new ItemStack(Material.MAP));

	}
	
	@Override
	public void GiveItems(Player player)
	{

	}
}
