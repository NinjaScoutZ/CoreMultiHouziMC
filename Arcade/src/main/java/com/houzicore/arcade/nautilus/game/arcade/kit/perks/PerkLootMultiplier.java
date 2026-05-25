package com.houzicore.arcade.nautilus.game.arcade.kit.perks;

import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.arcade.nautilus.game.arcade.kit.Perk;

import java.util.HashSet;

public class PerkLootMultiplier extends Perk
{
	// Track which chests we've already multiplied so we don't multiply them again
	private HashSet<Block> _multipliedChests = new HashSet<>();

	public PerkLootMultiplier() 
	{
		super("Loot Multiplier", new String[] 
		{ 
			"30% chance to double items when opening chests"
		});
	}
		
	@EventHandler(priority = EventPriority.MONITOR)
	public void onChestOpen(InventoryOpenEvent event)
	{
		if (event.isCancelled())
			return;
			
		Player player = (Player) event.getPlayer();
		
		if (!Kit.HasKit(player))
			return;
			
		InventoryHolder holder = event.getInventory().getHolder();
		if (!(holder instanceof Chest))
			return;
			
		Chest chest = (Chest) holder;
		Block block = chest.getBlock();
		
		// Only multiply once per chest
		if (_multipliedChests.contains(block))
			return;
			
		_multipliedChests.add(block);
		
		boolean multiplied = false;
		
		for (int i = 0; i < chest.getInventory().getSize(); i++)
		{
			ItemStack item = chest.getInventory().getItem(i);
			if (item == null || item.getType() == org.bukkit.Material.AIR)
				continue;
				
			if (UtilMath.r(100) < 30) // 30% chance
			{
				item.setAmount(item.getAmount() * 2);
				chest.getInventory().setItem(i, item);
				multiplied = true;
			}
		}
		
		if (multiplied)
		{
			player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 2.0f);
		}
	}
}
