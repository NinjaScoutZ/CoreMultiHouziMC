package com.houzicore.shared.core.itemstack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
//import org.bukkit.potion.Potion;

public class ItemBuilder {

	private static ArrayList<String> split(String string, int maxLength) {
		final String[] split = string.split(" ");
		string = "";
		final ArrayList<String> newString = new ArrayList<>();
		for (final String element : split) {
			string += (string.length() == 0 ? "" : " ") + element;
			if (ChatColor.stripColor(string).length() > maxLength) {
				newString.add((newString.size() > 0 ? ChatColor.getLastColors(newString.get(newString.size() - 1)) : "")
						+ string);
				string = "";
			}
		}
		if (string.length() > 0) {
			newString.add((newString.size() > 0 ? ChatColor.getLastColors(newString.get(newString.size() - 1)) : "")
					+ string);
		}
		return newString;
	}

	private int _amount;
	private Color _color;
	private short _data;
	private final HashMap<Enchantment, Integer> _enchants = new HashMap<>();
	private final List<String> _lore = new ArrayList<>();
	private Material _mat;
	// private Potion potion;
	private String _title = null;
	private boolean _unbreakable;
	private String _playerHeadName = null;
	private boolean _glow = false;
	private boolean _hideInfo = false;

	public ItemBuilder(ItemStack item) {
		this(item.getType(), item.getDurability());
		_amount = item.getAmount();
		_enchants.putAll(item.getEnchantments());
		if (item.getType() == Material.POTION) {
			// setPotion(Potion.fromItemStack(item));
		}
		if (item.hasItemMeta()) {
			final ItemMeta meta = item.getItemMeta();
			if (meta.hasDisplayName()) {
				_title = meta.getDisplayName();
			}
			if (meta.hasLore()) {
				_lore.addAll(meta.getLore());
			}
			if (meta instanceof LeatherArmorMeta) {
				setColor(((LeatherArmorMeta) meta).getColor());
			}
			_unbreakable = meta.isUnbreakable();
		}
	}

	public ItemBuilder(Material mat) {
		this(mat, 1);
	}

	public ItemBuilder(Material mat, int amount) {
		this(mat, amount, (short) 0);
	}

	public ItemBuilder(Material mat, int amount, short data) {
		_mat = mat;
		_amount = amount;
		_data = data;
	}

	public ItemBuilder(Material mat, short data) {
		this(mat, 1, data);
	}

	public ItemBuilder addEnchantment(Enchantment enchant, int level) {
		if (_enchants.containsKey(enchant)) {
			_enchants.remove(enchant);
		}
		_enchants.put(enchant, level);
		return this;
	}

	public ItemBuilder addLore(String... lores) {
		for (final String lore : lores) {
			_lore.add(ChatColor.GRAY + lore);
		}
		return this;
	}

	public ItemBuilder addLore(String lore, int maxLength) {
		_lore.addAll(split(lore, maxLength));
		return this;
	}

	public ItemBuilder addLore(String lore, com.houzicore.shared.common.util.LineFormat format) {
		_lore.addAll(split(lore, format.getLength()));
		return this;
	}

	public ItemBuilder addLores(List<String> lores) {
		_lore.addAll(lores);
		return this;
	}

	public ItemBuilder addLores(List<String> lores, int maxLength) {
		for (final String lore : lores) {
			addLore(lore, maxLength);
		}
		return this;
	}

	public ItemBuilder addLores(List<String> lores, com.houzicore.shared.common.util.LineFormat format) {
		for (final String lore : lores) {
			addLore(lore, format);
		}
		return this;
	}

	public ItemBuilder addLores(String[] description, int maxLength) {
		return addLores(Arrays.asList(description), maxLength);
	}

	public ItemBuilder addLores(String[] description, com.houzicore.shared.common.util.LineFormat format) {
		return addLores(Arrays.asList(description), format);
	}

	public ItemStack build() {
		Material mat = _mat;
		if (mat == null) {
			mat = Material.AIR;
			Bukkit.getLogger().warning("Null material!");
		} else if (mat == Material.AIR) {
			Bukkit.getLogger().warning("Air material!");
		}
		final ItemStack item = new ItemStack(mat, _amount, _data);
		final ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			if (_title != null) {
				meta.setDisplayName(_title);
			}
			if (!_lore.isEmpty()) {
				meta.setLore(_lore);
			}
			if (meta instanceof LeatherArmorMeta) {
				((LeatherArmorMeta) meta).setColor(_color);
			} else if (meta instanceof SkullMeta && _playerHeadName != null) {
				org.bukkit.entity.Player onlineTarget = org.bukkit.Bukkit.getPlayerExact(_playerHeadName);
				if (onlineTarget != null && onlineTarget.isOnline()) {
					((SkullMeta) meta).setPlayerProfile(onlineTarget.getPlayerProfile());
				} else {
					((SkullMeta) meta).setOwningPlayer(org.bukkit.Bukkit.getOfflinePlayer(_playerHeadName));
				}
			}
			meta.setUnbreakable(isUnbreakable());

			if (_glow) {
				meta.addEnchant(org.bukkit.enchantments.Enchantment.LUCK_OF_THE_SEA, 1, true);
				meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
			}

			if (_hideInfo) {
				meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES,
						org.bukkit.inventory.ItemFlag.HIDE_DESTROYS,
						org.bukkit.inventory.ItemFlag.HIDE_PLACED_ON,
						org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE);
			}

			item.setItemMeta(meta);
		}
		item.addUnsafeEnchantments(_enchants);
		// if (this.potion != null) {
		// this.potion.apply(item);
		// }
		return item;
	}

	@Override
	public ItemBuilder clone() {
		final ItemBuilder newBuilder = new ItemBuilder(_mat);

		newBuilder.setTitle(_title);
		for (final String lore : _lore) {
			newBuilder.addLore(lore);
		}
		for (final Map.Entry<Enchantment, Integer> entry : _enchants.entrySet()) {
			newBuilder.addEnchantment(entry.getKey(), entry.getValue());
		}
		newBuilder.setColor(_color);
		newBuilder.setGlow(_glow);
		newBuilder.setHideInfo(_hideInfo);
		// newBuilder.potion = this.potion;

		return newBuilder;
	}

	public HashMap<Enchantment, Integer> getAllEnchantments() {
		return _enchants;
	}

	public Color getColor() {
		return _color;
	}

	public short getData() {
		return _data;
	}

	public int getEnchantmentLevel(Enchantment enchant) {
		return _enchants.get(enchant);
	}

	public List<String> getLore() {
		return _lore;
	}

	public String getTitle() {
		return _title;
	}

	public Material getType() {
		return _mat;
	}

	public boolean hasEnchantment(Enchantment enchant) {
		return _enchants.containsKey(enchant);
	}

	public boolean isItem(ItemStack item) {
		final ItemMeta meta = item.getItemMeta();
		if (item.getType() != getType())
			return false;
		if (!meta.hasDisplayName() && getTitle() != null)
			return false;
		if (!meta.getDisplayName().equals(getTitle()))
			return false;
		if (!meta.hasLore() && !getLore().isEmpty())
			return false;
		if (meta.hasLore()) {
			for (final String lore : meta.getLore()) {
				if (!getLore().contains(lore))
					return false;
			}
		}
		for (final Enchantment enchant : item.getEnchantments().keySet()) {
			if (!hasEnchantment(enchant))
				return false;
		}
		return true;
	}

	public boolean isUnbreakable() {
		return _unbreakable;
	}

	public ItemBuilder setAmount(int amount) {
		_amount = amount;
		return this;
	}

	public ItemBuilder setColor(Color color) {
		if (!_mat.name().contains("LEATHER_"))
			throw new IllegalArgumentException("Can only dye leather armor!");
		_color = color;
		return this;
	}

	public void setData(short newData) {
		_data = newData;
	}

	public ItemBuilder setPlayerHead(String playerName) {
		_playerHeadName = playerName;
		return this;
	}

	public ItemBuilder setGlow(boolean glow) {
		_glow = glow;
		return this;
	}

	public ItemBuilder setHideInfo(boolean hideInfo) {
		_hideInfo = hideInfo;
		return this;
	}

	/* Potion set removed */

	public ItemBuilder setRawTitle(String title) {
		_title = title;
		return this;
	}

	public ItemBuilder setTitle(String title) {
		_title = (title == null ? null
				: title.length() > 2 && ChatColor.getLastColors(title.substring(0, 2)).length() == 0 ? ChatColor.WHITE
						: "")
				+ title;
		return this;
	}

	public ItemBuilder setTitle(String title, int maxLength) {
		if (title != null && ChatColor.stripColor(title).length() > maxLength) {
			final ArrayList<String> lores = split(title, maxLength);
			for (int i = 1; i < lores.size(); i++) {
				_lore.add(lores.get(i));
			}
			title = lores.get(0);
		}
		setTitle(title);
		return this;
	}

	public ItemBuilder setType(Material mat) {
		_mat = mat;
		return this;
	}

	public ItemBuilder setUnbreakable(boolean setUnbreakable) {
		_unbreakable = setUnbreakable;
		return this;
	}

}
