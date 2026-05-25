package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.ui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.page.ShopPageBase;

import com.houzicore.arcade.ArcadeManager;

public class BedwarsResourceShop extends ShopBase<ArcadeManager>
{

	public BedwarsResourceShop(ArcadeManager plugin)
	{
		super(plugin, plugin.GetClients(), plugin.GetDonation(), "Bed Wars Shop");
	}

	@EventHandler
	public void inventoryClick(InventoryClickEvent event)
	{
		if (event.getWhoClicked() instanceof Player && isPlayerInShop((Player) event.getWhoClicked()))
		{
			event.setCancelled(true);
		}
	}

	@Override
	protected ShopPageBase<ArcadeManager, ? extends ShopBase<ArcadeManager>> buildPagesFor(Player player)
	{
		return null;
	}
}
