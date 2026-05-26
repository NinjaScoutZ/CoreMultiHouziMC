package com.houzicore.shared.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemBuilder {
	private ItemStack item;
	private ItemMeta meta;

	public ItemBuilder(Material material) {
		this.item = new ItemStack(material);
		if (this.item.getItemMeta() != null) {
			this.meta = this.item.getItemMeta();
		}
	}

	public ItemBuilder(Material material, int amount) {
		this.item = new ItemStack(material, amount);
		if (this.item.getItemMeta() != null) {
			this.meta = this.item.getItemMeta();
		}
	}

	public ItemBuilder(ItemStack item) {
		this.item = item.clone();
		if (this.item.getItemMeta() != null) {
			this.meta = this.item.getItemMeta();
		}
	}

	public ItemBuilder setTitle(String title) {
		if (meta != null) meta.setDisplayName(title);
		return this;
	}

	public ItemBuilder setTitleComponent(net.kyori.adventure.text.Component component) {
		if (meta != null) meta.displayName(component);
		return this;
	}

	public ItemBuilder setLoreComponents(List<net.kyori.adventure.text.Component> loreComponents) {
		if (meta != null) meta.lore(loreComponents);
		return this;
	}

	public ItemBuilder addLore(String... lore) {
		if (meta != null) {
			List<String> currentLore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
			currentLore.addAll(Arrays.asList(lore));
			meta.setLore(currentLore);
		}
		return this;
	}

	public ItemBuilder setLore(String... lore) {
		if (meta != null) meta.setLore(Arrays.asList(lore));
		return this;
	}

	public ItemBuilder addGlow() {
		if (meta != null) {
			item.setItemMeta(meta);
			UtilInv.addDullEnchantment(item);
			meta = item.getItemMeta();
		}
		return this;
	}
	
	public ItemBuilder setUnbreakable(boolean unbreakable) {
		if (meta != null) meta.setUnbreakable(unbreakable);
		return this;
	}

	public ItemBuilder hideFlags() {
		if (meta != null) {
			meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_DESTROYS,
					ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_UNBREAKABLE);
		}
		return this;
	}

	public ItemStack build() {
		if (meta != null) {
			item.setItemMeta(meta);
		}
		return item;
	}
}
