package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.updater.UpdateType;

public enum BedwarsResource
{

	BRICK("Iron", ChatColor.GRAY, new ItemStack(Material.IRON_INGOT), UpdateType.TWOSEC, 64),
	EMERALD("Emerald", ChatColor.GREEN, new ItemStack(Material.EMERALD), UpdateType.SEC_05, 24),
	STAR("Diamond", ChatColor.AQUA, new ItemStack(Material.DIAMOND), UpdateType.SEC_08, 16);

	private final String _name;
	private final ChatColor _chatColor;
	private final ItemStack _itemStack;
	private final UpdateType _spawnerUpdate;
	private final int _maxSpawned;

	BedwarsResource(String name, ChatColor chatColor, ItemStack itemStack, UpdateType spawnerUpdate, int maxSpawned)
	{
		_name = name;
		_chatColor = chatColor;
		_itemStack = itemStack;
		_spawnerUpdate = spawnerUpdate;
		_maxSpawned = maxSpawned;
	}

	public String getName()
	{
		return _name;
	}

	public ChatColor getChatColor()
	{
		return _chatColor;
	}

	public ItemStack getItemStack()
	{
		return _itemStack;
	}

	public UpdateType getSpawnerUpdate()
	{
		return _spawnerUpdate;
	}

	public int getMaxSpawned()
	{
		return _maxSpawned;
	}
}
