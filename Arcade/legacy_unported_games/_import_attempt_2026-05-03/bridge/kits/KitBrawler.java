package com.houzicore.arcade.nautilus.game.arcade.game.games.bridge.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.arcade.nautilus.game.arcade.kit.GameKit;
import com.houzicore.shared.core.itemstack.ItemStackFactory;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkIronSkin;
import com.houzicore.arcade.nautilus.game.arcade.kit.perks.PerkMammoth;

public class KitBrawler extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkMammoth(),
					new PerkIronSkin(1)
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.IRON_SWORD)
			};

	public KitBrawler(ArcadeManager manager)
	{
		super(manager, GameKit.BRIDGES_BRAWLER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
	}
}

