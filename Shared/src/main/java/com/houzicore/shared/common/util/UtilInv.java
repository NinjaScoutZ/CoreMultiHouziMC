package com.houzicore.shared.common.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class UtilInv
{
	static 
	{
		// Legacy DullEnchantment replaced by 1.8+ ItemFlag.HIDE_ENCHANTS
	}
	
	public static void addDullEnchantment(ItemStack itemStack)
	{
		itemStack.addUnsafeEnchantment(Enchantment.LURE, 1);
		org.bukkit.inventory.meta.ItemMeta meta = itemStack.getItemMeta();
		if (meta != null) {
			meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
			itemStack.setItemMeta(meta);
		}
	}
	
	public static void removeDullEnchantment(ItemStack itemStack)
	{
	    itemStack.removeEnchantment(Enchantment.LURE);
	}
	
	public static DullEnchantment getDullEnchantment()
	{
	    return null;
	}
	
	@SuppressWarnings("deprecation")
	public static boolean insert(Player player, ItemStack stack)
	{
		//CHECK IF FIT
		
		//Insert
		player.getInventory().addItem(stack);
		player.updateInventory();
		return true;
	}
	
	public static boolean contains(Player player, Material item, byte data, int required)
	{
		return contains(player, null, item, data, required);
	}

	public static boolean contains(Player player, String itemNameContains, Material item, byte data, int required)
	{
		return contains(player, itemNameContains, item, data, required, true, true);
	}

	public static boolean contains(Player player, String itemNameContains, Material item, byte data, int required, boolean checkArmor, boolean checkCursor)
	{
		
		for (ItemStack stack : getItems(player, checkArmor, checkCursor))
		{
			if (required <= 0)
			{
				return true;
			}
			
			if (stack == null)
				continue;
			
			if (stack.getType() != item)
				continue;
			
			if (stack.getAmount() <= 0)
				continue;
			
			if (data >=0 && 
				IdUtil.getData(stack) != data)
				continue;
			
			if (itemNameContains != null && 
				(stack.getItemMeta().getDisplayName() == null || !stack.getItemMeta().getDisplayName().contains(itemNameContains)))
				continue;
			
			required -= stack.getAmount();
		}
		
		if (required <= 0)
		{
			return true;
		}
		
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public static boolean remove(Player player, Material item, byte data, int toRemove) 
	{
		if (!contains(player, item, data, toRemove))
			return false;
		
		for (int i : player.getInventory().all(item).keySet()) 
		{
			if (toRemove <= 0)
				continue;
			
			ItemStack stack = player.getInventory().getItem(i);

			if (IdUtil.getData(stack) == data)
			{
				int foundAmount = stack.getAmount();

				if (toRemove >= foundAmount) 
				{
					toRemove -= foundAmount;
					player.getInventory().setItem(i, null);
				} 

				else 
				{
					stack.setAmount(foundAmount - toRemove);
					player.getInventory().setItem(i, stack);
					toRemove = 0;
				}
			} 
		}
		
		player.updateInventory();
		return true;
	}

	public static void Clear(Player player)
	{
		//player.getOpenInventory().close();
		
		PlayerInventory inv = player.getInventory();
		
		inv.clear();
        inv.setArmorContents(new ItemStack[4]);
	    player.setItemOnCursor(new ItemStack(Material.AIR));
		
		player.saveData();
	}

	public static ArrayList<ItemStack> getItems(Player player)
	{
		return getItems(player, true, true);
	}

	public static ArrayList<ItemStack> getItems(Player player, boolean getArmor, boolean getCursor)
	{
		ArrayList<ItemStack> items = new ArrayList<ItemStack>();
		PlayerInventory inv = player.getInventory();

		for (ItemStack item : inv.getContents())
		{
			if (item != null && item.getType() != Material.AIR)
			{
				items.add(item.clone());
			}
		}

		if (getArmor)
		{
			for (ItemStack item : inv.getArmorContents())
			{
				if (item != null && item.getType() != Material.AIR)
				{
					items.add(item.clone());
				}
			}
		}

		if (getCursor)
		{
			ItemStack cursorItem = player.getItemOnCursor();

			if (cursorItem != null && cursorItem.getType() != Material.AIR)
				items.add(cursorItem.clone());
		}

		return items;
	}
	
	public static void drop(Player player, boolean clear)
	{
		for (ItemStack cur : getItems(player))
		{
			player.getWorld().dropItemNaturally(player.getLocation(), cur);
		}
		
		if (clear)
			Clear(player);
	}

	@SuppressWarnings("deprecation")
	public static void Update(Entity player) 
	{
		if (!(player instanceof Player))
			return;
		
		((Player)player).updateInventory();
	}

	public static int countItems(Player player, Material type, byte data)
	{
		int count = 0;
		for (ItemStack item : player.getInventory().getContents())
		{
			if (item != null && item.getType() == type)
			{
				if (data == -1 || data == 0 || IdUtil.getData(item) == data)
				{
					count += item.getAmount();
				}
			}
		}
		return count;
	}

	public static int removeAll(Player player, Material type, byte data) 
	{
		HashSet<ItemStack> remove = new HashSet<ItemStack>();
		int count = 0;
		
		for (ItemStack item : player.getInventory().getContents())
			if (item != null)
				if (item.getType() == type)
				if (data == -1 || data == 0 || IdUtil.getData(item) == data)
					{
						count += item.getAmount();
						remove.add(item);
					}
	
		for (ItemStack item : remove)
			player.getInventory().remove(item);	

		return count;
	}
	
	public static byte GetData(ItemStack stack)
	{
		if (stack == null)
			return (byte)0;
		
		return IdUtil.getData(stack);
	}

	public static boolean IsItem(ItemStack item, Material type, byte data)
	{
		return IsItem(item, null, type, data);
	}
	
	public static boolean IsItem(ItemStack item, String name, Material type, byte data)
	{
		if (item == null)
			return false;

		if (item.getType() != type)
			return false;

		if (data != -1 && data != 0 && GetData(item) != data)
			return false;

		if (name != null && (item.getItemMeta() == null || item.getItemMeta().getDisplayName() == null || !item.getItemMeta().getDisplayName().contains(name)))
			return false;

		return true;
	}
	
	@Deprecated
	public static boolean IsItem(ItemStack item, String name, int id, byte data)
	{
		if (item == null)
			return false;
		
		if (com.houzicore.shared.common.util.IdUtil.getTypeId(item) != id)
			return false;
		
		if (data != -1 && data != 0 && GetData(item) != data)
			return false;
		
		if (name != null && (item.getItemMeta() == null || item.getItemMeta().getDisplayName() == null || !item.getItemMeta().getDisplayName().contains(name)))
			return false;
		
		return true;
	}
	
	public static void DisallowMovementOf(InventoryClickEvent event, String name, Material type, byte data, boolean inform) 
	{
		DisallowMovementOf(event, name, type, data, inform, false);
	}
	
	public static void DisallowMovementOf(InventoryClickEvent event, String name, Material type, byte data, boolean inform, boolean allInventorties) 
	{
		/*
		 
		
		
		*/
		
		//Do what you want in Crafting Inv
		if (!allInventorties && event.getInventory().getType() == InventoryType.CRAFTING)
			return;
		
		//Hotbar Swap
		if (event.getAction() == InventoryAction.HOTBAR_SWAP ||
			event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD)
		{
			boolean match = false;
			
			if (IsItem(event.getCurrentItem(), name, type, data))
				match = true;

			if (IsItem(event.getWhoClicked().getInventory().getItem(event.getHotbarButton()), name, type, data))
				match = true;
			
			if (!match) 
				return; 
			
			//Inform
			UtilPlayer.message(event.getWhoClicked(), F.main("Inventory", "You cannot hotbar swap " + F.item(name) + "."));
			event.setCancelled(true);
		}
		//Other
		else
		{
			if (event.getCurrentItem() == null)
				return;

			IsItem(event.getCurrentItem(), name, type, data);
			
			//Type
			if (!IsItem(event.getCurrentItem(), name, type, data))
				return;
			//Inform
			UtilPlayer.message(event.getWhoClicked(), F.main("Inventory", "You cannot move " + F.item(name) + "."));
			event.setCancelled(true);
		}
	}

	public static void UseItemInHand(Player player)
	{
		if (player.getItemInHand().getAmount() > 1)
			player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
		else
			player.setItemInHand(null);
		
		Update(player);
	}
	
	
}
