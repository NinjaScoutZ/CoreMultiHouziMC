package com.houzicore.arcade.nautilus.game.arcade.game.games.event.kits;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.games.event.EventGame;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

public class KitPlayer extends Kit
{
	public KitPlayer(ArcadeManager manager)
	{
		super(manager, "Party Animal", KitAvailability.Free, 0,

				// EN
				new String[] 
						{
				""
						}, 
				// TH
				new String[] 
						{
				""
						}, 
				new Perk[] 
								{
								}, 
								EntityType.PIG,
								new ItemStack(Material.PORKCHOP));

	}
	
	@Override
	public void GiveItems(Player player) 
	{
		((EventGame)Manager.GetGame()).giveItems(player);
	}
}
