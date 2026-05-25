package com.houzicore.shared.core.shop.page;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.core.shop.item.IButton;

/**
 * A lightweight, single-shot GUI builder designed for simple prompts or voting menus
 * without the heavy dependency footprint of the full ShopBase hierarchy.
 */
public abstract class SimpleGui implements InventoryHolder {

	private final Player _player;
	private final Inventory _inventory;
	private final Map<Integer, IButton> _buttons = new HashMap<>();

	public SimpleGui(Player player, String title, int size) {
		_player = player;
		_inventory = Bukkit.createInventory(this, size, title);
	}

	public void open() {
		buildPage();
		_player.openInventory(_inventory);
	}

	protected abstract void buildPage();

	protected void setItem(int slot, ItemStack item, IButton button) {
		_inventory.setItem(slot, item);
		if (button != null) {
			_buttons.put(slot, button);
		}
	}

	public void handleClick(InventoryClickEvent event) {
		if (event.getClickedInventory() != null && event.getClickedInventory().equals(_inventory)) {
			event.setCancelled(true);
			IButton action = _buttons.get(event.getSlot());
			if (action != null) {
				action.onClick(_player, event.getClick());
			}
		}
	}
	
	public void handleClose(InventoryCloseEvent event) {
		onClose();
		_buttons.clear();
	}

	protected void onClose() {}

	@Override
	public Inventory getInventory() {
		return _inventory;
	}

	public Player getPlayer() {
		return _player;
	}
}
