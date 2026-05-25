package com.houzicore.arcade.nautilus.game.arcade.game.games.primalgames.kit;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.KitAvailability;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.shared.core.itemstack.ItemBuilder;

public class KitAssassin extends Kit
{
	public KitAssassin(ArcadeManager manager)
	{
		super(manager, "Assassin", KitAvailability.Gem, 5000,

		new String[]
			{
					"A master of stealth and quick escapes.",
					"Killing an enemy grants you temporary speed and invisibility."
			},

		new Perk[]
			{
				new PerkAssassin(),
			}, EntityType.ZOMBIE, new ItemStack(Material.IRON_SWORD));
	}

	@Override
	public void GiveItems(Player player)
	{

	}
}
