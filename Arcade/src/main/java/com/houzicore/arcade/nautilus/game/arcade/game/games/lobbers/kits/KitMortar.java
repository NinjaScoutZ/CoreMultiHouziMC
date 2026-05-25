package com.houzicore.arcade.nautilus.game.arcade.game.games.lobbers.kits;

import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.games.lobbers.kits.perks.PerkCraftman;
import com.houzicore.arcade.nautilus.game.arcade.game.games.lobbers.kits.perks.PerkMortar;
import com.houzicore.arcade.nautilus.game.arcade.game.games.lobbers.kits.perks.PerkMorterCraftman;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class KitMortar extends Kit
{
	public KitMortar(ArcadeManager manager)
	{
		super(manager, "Mortar", KitAvailability.Gem, 6000, // EN
				new String[]
				{
				"He loves the big guns."
				}, 
				// TH
				new String[]
				{
				"[TH] He loves the big guns."
				}, 
				new Perk[]
						{
				new PerkMortar(),
				new PerkCraftman(),
				new PerkMorterCraftman()
						}, EntityType.ZOMBIE, new ItemBuilder(Material.FIRE_CHARGE).build());
	}

	@Override
	public void GiveItems(Player player)
	{
		
	}
}
