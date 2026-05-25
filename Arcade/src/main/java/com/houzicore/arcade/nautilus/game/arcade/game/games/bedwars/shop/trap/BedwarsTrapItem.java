package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.trap;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.itemstack.ItemBuilder;

import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.BedwarsShopItem;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.BedwarsShopItemType;

public abstract class BedwarsTrapItem extends BedwarsShopItem
{

	private final String _name;
	TrapTrigger _trapTrigger;

	BedwarsTrapItem(ItemStack itemStack, int cost, String name, String... description)
	{
		super(BedwarsShopItemType.TRAP, new ItemBuilder(itemStack)
				.setTitle(C.cYellow + C.Bold + name)
				.addLore(description)
				.build(), cost);

		_name = name;
		_trapTrigger = TrapTrigger.BED_INTERACT;
	}

	public abstract void onTrapTrigger(Player player, Location bed);

	public String getName()
	{
		return _name;
	}

	public TrapTrigger getTrapTrigger()
	{
		return _trapTrigger;
	}

	public enum TrapTrigger
	{
		BED_NEAR,
		BED_INTERACT,
	}
}
