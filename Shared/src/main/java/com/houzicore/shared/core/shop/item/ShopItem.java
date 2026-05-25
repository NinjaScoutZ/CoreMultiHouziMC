package com.houzicore.shared.core.shop.item;

import java.util.ArrayList;

import org.bukkit.Material;
////
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilInv;

public class ShopItem extends ItemStack {
	protected String _name;
	private final String _deliveryName;
	protected String[] _lore;
	private final int _deliveryAmount;
	private boolean _locked;
	private final boolean _displayItem;

	@SuppressWarnings("deprecation")
	private static Material safeMaterial(int id) {
		for (Material m : Material.values()) {
			if (!m.isLegacy() && m.ordinal() == id) return m;
		}
		return Material.STONE;
	}

	public ShopItem(int type, byte data, String name, String deliveryName, String[] lore, int deliveryAmount,
			boolean locked, boolean displayItem) {
		super(safeMaterial(type), Math.max(deliveryAmount, 1));

		_name = name;
		_deliveryName = deliveryName;
		_lore = lore;
		_displayItem = displayItem;
		_deliveryAmount = deliveryAmount;
		_locked = locked;

		UpdateVisual(false);

		setAmount(Math.max(deliveryAmount, 1));
	}

	public ShopItem(int type, byte data, String name, String[] lore, int deliveryAmount, boolean locked,
			boolean displayItem) {
		this(type, data, name, null, lore, deliveryAmount, locked, displayItem);
	}

	public ShopItem(int type, String name, int deliveryAmount, boolean locked) {
		this(type, name, null, deliveryAmount, locked);
	}

	public ShopItem(int type, String name, String[] lore, int deliveryAmount, boolean locked) {
		this(type, name, lore, deliveryAmount, locked, false);
	}

	public ShopItem(int type, String name, String[] lore, int deliveryAmount, boolean locked, boolean displayItem) {
		this(type, (byte) 0, name, null, lore, deliveryAmount, locked, displayItem);
	}

	public ShopItem(ItemStack itemStack, String name, String deliveryName, int deliveryAmount, boolean locked,
			boolean displayItem) {
		super(itemStack);

		_name = name;
		_deliveryName = deliveryName;
		_displayItem = displayItem;
		_deliveryAmount = deliveryAmount;
		if (itemStack.getItemMeta().hasLore()) {
			_lore = itemStack.getItemMeta().getLore().toArray(new String[0]);
		} else {
			_lore = new String[0];
		}

		UpdateVisual(true);
	}

	public ShopItem(Material type, byte data, String name, String deliveryName, String[] lore, int deliveryAmount,
			boolean locked, boolean displayItem) {
		super(type == null || type == Material.AIR ? Material.BARRIER : type, Math.max(deliveryAmount, 1));

		_name = name;
		_deliveryName = deliveryName;
		_lore = lore;
		_displayItem = displayItem;
		_deliveryAmount = deliveryAmount;
		_locked = locked;

		UpdateVisual(false);

		setAmount(Math.max(deliveryAmount, 1));
	}

	public ShopItem(Material type, byte data, String name, String[] lore, int deliveryAmount, boolean locked,
			boolean displayItem) {
		this(type, data, name, null, lore, deliveryAmount, locked, displayItem);
	}

	public ShopItem(Material type, String name, int deliveryAmount, boolean locked) {
		this(type, name, null, deliveryAmount, locked);
	}

	public ShopItem(Material type, String name, String[] lore, int deliveryAmount, boolean locked) {
		this(type, name, lore, deliveryAmount, locked, false);
	}

	public ShopItem(Material type, String name, String[] lore, int deliveryAmount, boolean locked,
			boolean displayItem) {
		this(type, (byte) 0, name, null, lore, deliveryAmount, locked, displayItem);
	}

	public void addGlow() {
		UtilInv.addDullEnchantment(this);
	}

	@Override
	public ShopItem clone() {
		return new ShopItem(super.clone(), _name, _deliveryName, _deliveryAmount, _locked, _displayItem);
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	// getHandle removed

	public String GetName() {
		return _name;
	}

	public boolean IsDisplay() {
		return _displayItem;
	}

	public boolean IsLocked() {
		return _locked;
	}

	public void SetDeliverySettings() {
		setAmount(_deliveryAmount);

		// Delivery Name
		if (_deliveryName != null) {
			final ItemMeta meta = getItemMeta();
			meta.setDisplayName(_deliveryName);
			setItemMeta(meta);
		}
	}

	public void SetLocked(boolean owns) {
		_locked = owns;
		UpdateVisual(false);
	}

	public void SetLore(String[] string) {
		_lore = string;

		final ArrayList<String> lore = new ArrayList<>();

		if (_lore != null) {
			for (final String line : _lore) {
				if (line != null && !line.isEmpty()) {
					lore.add(line);
				}
			}
		}

		final ItemMeta meta = getItemMeta();
		meta.setLore(lore);
		setItemMeta(meta);
	}

	public void SetName(String name) {
		_name = name;
	}

	protected void UpdateVisual(boolean clone) {
		final ItemMeta meta = getItemMeta();
		if (meta == null) return;
		if (!clone) {
			meta.setDisplayName((_locked && !_displayItem ? C.cRed : C.cGreen) + C.Bold + _name);
		}

		final ArrayList<String> lore = new ArrayList<>();

		if (_lore != null) {
			for (final String line : _lore) {
				if (line != null && !line.isEmpty()) {
					lore.add(line);
				}
			}
		}
		meta.setLore(lore);
		;

		setItemMeta(meta);
	}
}
