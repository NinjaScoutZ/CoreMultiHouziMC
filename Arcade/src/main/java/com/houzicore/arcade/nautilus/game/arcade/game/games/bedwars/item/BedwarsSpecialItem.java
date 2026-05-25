package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.item;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.common.util.UtilBlock;

import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.Bedwars;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.team.BedwarsTeam;

public abstract class BedwarsSpecialItem
{

	protected final Bedwars _game;
	private final ItemStack _itemStack;
	private final String _name;
	private final long _cooldown;

	public BedwarsSpecialItem(Bedwars game, ItemStack itemStack)
	{
		this(game, itemStack, "BedwarsSpecialItem", 0);
	}

	public BedwarsSpecialItem(Bedwars game, ItemStack itemStack, String name, long cooldown)
	{
		_game = game;
		_itemStack = itemStack;
		_name = name;
		_cooldown = cooldown;
	}

	protected abstract boolean onClick(PlayerInteractEvent event, BedwarsTeam bedTeam);

	protected void setup()
	{
	}

	protected void cleanup()
	{
	}

	protected boolean isInvalidBlock(Block block)
	{
		Location location = block.getLocation();
		return !UtilBlock.airFoliage(block) || _game.getCapturePointModule().isOnPoint(location) || _game.getBedwarsShopModule().isNearShop(location) || _game.getBedwarsSpawnerModule().isNearSpawner(block) || _game.isNearSpawn(block);
	}

	public ItemStack getItemStack()
	{
		return _itemStack;
	}

	public String getName()
	{
		return _name;
	}

	public long getCooldown()
	{
		return _cooldown;
	}
}
