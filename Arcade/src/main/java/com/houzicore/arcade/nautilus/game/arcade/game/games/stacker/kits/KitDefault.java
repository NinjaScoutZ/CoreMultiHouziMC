package com.houzicore.arcade.nautilus.game.arcade.game.games.stacker.kits;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.*;

public class KitDefault extends Kit
{
	public KitDefault(ArcadeManager manager)
	{
		super(manager, "Default", KitAvailability.Free, 

				// EN
				new String[] 
						{
						}, 
				// TH
				new String[] 
						{
						}, 
				new Perk[] 
								{ 
								}, 
								EntityType.ZOMBIE,
								new ItemStack(Material.OAK_BUTTON)); 

	}
	
	@Override
	public void GiveItems(Player player)
	{

	}
}
