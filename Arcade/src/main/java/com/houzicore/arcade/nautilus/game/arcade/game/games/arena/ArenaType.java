package com.houzicore.arcade.nautilus.game.arcade.game.games.arena;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum ArenaType {
	RED(1, new Loadout() {
		@Override public ItemStack getSword() { return new ItemStack(Material.DIAMOND_SWORD, 1); }
		@Override public ItemStack getRod() { return new ItemStack(Material.FISHING_ROD, 1); }
		@Override public ItemStack getBow() { return new ItemStack(Material.BOW, 1); }
		@Override public ItemStack getArrows() { return new ItemStack(Material.ARROW, 10); }
		@Override public ItemStack getHelmet() { return new ItemStack(Material.IRON_HELMET, 1); }
		@Override public ItemStack getChestplate() { return new ItemStack(Material.IRON_CHESTPLATE, 1); }
		@Override public ItemStack getLeggings() { return new ItemStack(Material.IRON_LEGGINGS, 1); }
		@Override public ItemStack getBoots() { return new ItemStack(Material.IRON_BOOTS, 1); }
	}),
	ORANGE(2, new Loadout() {
		@Override public ItemStack getSword() { return new ItemStack(Material.IRON_SWORD, 1); }
		@Override public ItemStack getRod() { return new ItemStack(Material.FISHING_ROD, 1); }
		@Override public ItemStack getBow() { return new ItemStack(Material.BOW, 1); }
		@Override public ItemStack getArrows() { return new ItemStack(Material.ARROW, 7); }
		@Override public ItemStack getHelmet() { return new ItemStack(Material.CHAINMAIL_HELMET); }
		@Override public ItemStack getChestplate() { return new ItemStack(Material.CHAINMAIL_CHESTPLATE, 1); }
		@Override public ItemStack getLeggings() { return new ItemStack(Material.CHAINMAIL_LEGGINGS, 1); }
		@Override public ItemStack getBoots() { return new ItemStack(Material.CHAINMAIL_BOOTS, 1); }
	}),
	YELLOW(4, new Loadout() {
		@Override public ItemStack getSword() { return new ItemStack(Material.STONE_SWORD, 1); }
		@Override public ItemStack getRod() { return new ItemStack(Material.FISHING_ROD, 1); }
		@Override public ItemStack getBow() { return new ItemStack(Material.BOW, 1); }
		@Override public ItemStack getArrows() { return new ItemStack(Material.ARROW, 5); }
		@Override public ItemStack getHelmet() { return new ItemStack(Material.GOLDEN_HELMET, 1); }
		@Override public ItemStack getChestplate() { return new ItemStack(Material.GOLDEN_CHESTPLATE, 1); }
		@Override public ItemStack getLeggings() { return new ItemStack(Material.GOLDEN_LEGGINGS, 1); }
		@Override public ItemStack getBoots() { return new ItemStack(Material.GOLDEN_BOOTS, 1); }
	}),
	GREEN(8, new Loadout() {
		@Override public ItemStack getSword() { return new ItemStack(Material.WOODEN_SWORD, 1); }
		@Override public ItemStack getRod() { return new ItemStack(Material.FISHING_ROD, 1); }
		@Override public ItemStack getBow() { return new ItemStack(Material.BOW, 1); }
		@Override public ItemStack getArrows() { return new ItemStack(Material.ARROW, 3); }
		@Override public ItemStack getHelmet() { return new ItemStack(Material.LEATHER_HELMET, 1); }
		@Override public ItemStack getChestplate() { return new ItemStack(Material.LEATHER_CHESTPLATE, 1); }
		@Override public ItemStack getLeggings() { return new ItemStack(Material.LEATHER_LEGGINGS, 1); }
		@Override public ItemStack getBoots() { return new ItemStack(Material.LEATHER_BOOTS, 1); }
	});

	private final int endsAt;
	private final Loadout loadout;

	ArenaType(int endsAt, Loadout loadout) {
		this.endsAt = endsAt;
		this.loadout = loadout;
	}

	public Loadout getLoadout() {
		return loadout;
	}

	public int getEndsAt() {
		return endsAt;
	}

	public String getName() {
		return name().toLowerCase();
	}

	public boolean furtherOut(ArenaType other) {
		return !(compareTo(other) <= 0);
	}
}
