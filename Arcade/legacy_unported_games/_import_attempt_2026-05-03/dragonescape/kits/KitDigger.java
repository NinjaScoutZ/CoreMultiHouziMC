package com.houzicore.arcade.nautilus.game.arcade.game.games.dragonescape.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.arcade.nautilus.game.arcade.kit.GameKit;
import com.houzicore.shared.core.itemstack.ItemStackFactory;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;

public class KitDigger extends Kit
{

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.DIAMOND_PICKAXE, 6)
			};

	public KitDigger(ArcadeManager manager)
	{
		super(manager, GameKit.DRAGON_ESCAPE_DIGGER);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
		player.setExp(0.99f);
	}
}

