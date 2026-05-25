package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.ui;

import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.shared.core.shop.page.ShopPageBase;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.GameTeam;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.Bedwars;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.BedwarsItem;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.BedwarsResource;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.BedwarsShopItemType;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.BedwarsShopModule;

public class BedwarsResourcePage extends ShopPageBase<ArcadeManager, BedwarsResourceShop>
{

	private static final ItemStack CLOSE_ITEM = new ItemBuilder(Material.BARRIER)
			.setTitle(C.cRed + C.Bold + "Close")
			.build();

	final Bedwars _game;
	final BedwarsResource _resource;
	final List<BedwarsItem> _items;
	final GameTeam _team;
	private BedwarsShopCategory _currentCategory = BedwarsShopCategory.QUICK_BUY;

	public BedwarsResourcePage(ArcadeManager plugin, BedwarsResourceShop shop, Player player, BedwarsResource resource, List<BedwarsItem> items)
	{
		this(plugin, shop, player, 54, resource, items);
	}

	public BedwarsResourcePage(ArcadeManager plugin, BedwarsResourceShop shop, Player player, int slots, BedwarsResource resource, List<BedwarsItem> items)
	{
		super(plugin, shop, plugin.GetClients(), plugin.GetDonation(), resource.getName() + " Shop", player, slots);

		_game = (Bedwars) plugin.GetGame();
		_resource = resource;
		_items = items;
		_team = plugin.GetGame().GetTeam(player);
	}

	@Override
	protected void buildPage()
	{
		// 1. Build Category Tabs in Row 0 (Slots 0-7)
		for (BedwarsShopCategory cat : BedwarsShopCategory.values())
		{
			int catSlot = cat.ordinal();
			ItemBuilder catItem = new ItemBuilder(cat.getMaterial())
					.setTitle((_currentCategory == cat ? "§a§l" : "§e§l") + cat.getName());
			if (_currentCategory == cat)
			{
				catItem.setGlow(true);
				catItem.addLore("", "§aCurrently Selected");
			}
			else
			{
				catItem.addLore("", "§7Click to view category");
			}
			addButton(catSlot, catItem.build(), (player, clickType) ->
			{
				if (_currentCategory != cat)
				{
					_currentCategory = cat;
					playAcceptSound(player);
					refresh();
				}
			});
		}

		// Close button at Slot 8
		addButton(8, CLOSE_ITEM, (player, clickType) -> player.closeInventory());

		// 2. Build Borders (Row 1, Row 5, and sides)
		for (int i = 9; i <= 17; i++)
		{
			addButton(i, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setTitle(" ").build(), (player, clickType) -> {});
		}
		for (int i = 45; i <= 53; i++)
		{
			if (i == 49) continue; // Resource count display
			addButton(i, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setTitle(" ").build(), (player, clickType) -> {});
		}

		int[] sides = {18, 26, 27, 35, 36, 44};
		for (int s : sides)
		{
			addButton(s, new ItemBuilder(Material.BLUE_STAINED_GLASS_PANE).setTitle(" ").build(), (player, clickType) -> {});
		}

		// 3. Resource Count Display at Slot 49
		int ownedResourceCount = UtilInv.countItems(getPlayer(), _resource.getItemStack().getType(), (byte) 0);
		addButton(49, new ItemBuilder(_resource.getItemStack().getType())
				.setTitle(_resource.getChatColor() + C.Bold + "You have: " + ownedResourceCount + " " + _resource.getName() + "s")
				.build(), (player, clickType) -> {});

		// 4. Fill Items Grid inside 3x7 area (slots 19-25, 28-34, 37-43)
		int[] gridSlots = {
			19, 20, 21, 22, 23, 24, 25,
			28, 29, 30, 31, 32, 33, 34,
			37, 38, 39, 40, 41, 42, 43
		};
		int gridIndex = 0;
		for (BedwarsItem item : _items)
		{
			if (belongsToCategory(item, _currentCategory))
			{
				if (gridIndex >= gridSlots.length) break;
				int targetSlot = gridSlots[gridIndex++];
				BedShopResult result = getResultPurchase(item, 1);
				addButton(targetSlot, prepareItem(item, result), new BedShopButton(item));
			}
		}
	}

	private boolean belongsToCategory(BedwarsItem item, BedwarsShopCategory category)
	{
		Material type = item.getItemStack().getType();
		BedwarsShopItemType itemType = item.getItemType();

		switch (category)
		{
			case QUICK_BUY:
				if (_resource == BedwarsResource.BRICK)
				{
					return type == Material.WHITE_WOOL 
						|| type == Material.IRON_SWORD 
						|| type == Material.IRON_CHESTPLATE 
						|| type == Material.BOW 
						|| type == Material.IRON_PICKAXE
						|| type == Material.ARROW;
				}
				else // EMERALD
				{
					return type == Material.DIAMOND_SWORD 
						|| type == Material.DIAMOND_CHESTPLATE 
						|| type == Material.OBSIDIAN 
						|| type == Material.ENDER_PEARL 
						|| type == Material.GOLDEN_APPLE
						|| type == Material.TNT;
				}

			case BLOCKS:
				return itemType == BedwarsShopItemType.BLOCK || type.name().contains("WOOL") || type.name().contains("TERRACOTTA") || type.name().contains("PLANKS") || type == Material.END_STONE || type == Material.OBSIDIAN;

			case MELEE:
				return itemType == BedwarsShopItemType.SWORD || type.name().contains("SWORD");

			case ARMOR:
				return itemType == BedwarsShopItemType.HELMET || itemType == BedwarsShopItemType.CHESTPLATE || itemType == BedwarsShopItemType.LEGGINGS || itemType == BedwarsShopItemType.BOOTS;

			case TOOLS:
				return itemType == BedwarsShopItemType.PICKAXE || itemType == BedwarsShopItemType.AXE || itemType == BedwarsShopItemType.SHEARS || type == Material.SHEARS || type.name().contains("PICKAXE") || type.name().contains("AXE");

			case RANGED:
				return itemType == BedwarsShopItemType.BOW || type == Material.BOW || type == Material.ARROW || type == Material.CROSSBOW;

			case POTIONS:
				return type == Material.POTION || type == Material.SPLASH_POTION || type == Material.LINGERING_POTION || (item.getItemStack().hasItemMeta() && item.getItemStack().getItemMeta().hasDisplayName() && item.getItemStack().getItemMeta().getDisplayName().contains("Potion"));

			case UTILITY:
				return !belongsToCategory(item, BedwarsShopCategory.BLOCKS)
					&& !belongsToCategory(item, BedwarsShopCategory.MELEE)
					&& !belongsToCategory(item, BedwarsShopCategory.ARMOR)
					&& !belongsToCategory(item, BedwarsShopCategory.TOOLS)
					&& !belongsToCategory(item, BedwarsShopCategory.RANGED)
					&& !belongsToCategory(item, BedwarsShopCategory.POTIONS);
		}

		return false;
	}

	BedShopResult getResultPurchase(BedwarsItem item, int multiplier)
	{
		BedwarsShopModule module = _game.getBedwarsShopModule();
		BedwarsShopItemType itemType = item.getItemType();
		ItemStack itemStack = _resource.getItemStack();
 
		if (!itemType.isMultiBuy() && (module.ownsItem(getPlayer(), item) || containsLowerTier(getPlayer(), item.getItemStack())))
		{
			return BedShopResult.ALREADY_OWNED;
		}
		else if (itemType.isOnePerTeam() && module.ownsItem(_team, item))
		{
			return BedShopResult.ONLY_ONE;
		}
		else if (!UtilInv.contains(getPlayer(), null, itemStack.getType(), (byte) 0, getCost(item, multiplier), false, false))
		{
			return BedShopResult.NOT_ENOUGH_RESOURCES;
		}
 
		return BedShopResult.SUCCESSFUL;
	}

	private boolean containsLowerTier(Player player, ItemStack itemStack)
	{
		PlayerInventory inventory = player.getInventory();

		switch (itemStack.getType())
		{
			case IRON_HELMET:
				return inventory.getHelmet() != null && inventory.getHelmet().getType() == Material.DIAMOND_HELMET;
			case IRON_CHESTPLATE:
				return inventory.getChestplate() != null && inventory.getChestplate().getType() == Material.DIAMOND_CHESTPLATE;
			case IRON_LEGGINGS:
				return inventory.getLeggings() != null && inventory.getLeggings().getType() == Material.DIAMOND_LEGGINGS;
			case IRON_BOOTS:
				return inventory.getBoots() != null && inventory.getBoots().getType() == Material.DIAMOND_BOOTS;
			case IRON_SWORD:
				return inventory.contains(Material.DIAMOND_SWORD);
			case IRON_PICKAXE:
				return inventory.contains(Material.DIAMOND_PICKAXE);
		}

		return false;
	}

	ItemStack prepareItem(BedwarsItem item, BedShopResult result)
	{
		ItemStack itemStack = item.getItemStack();
		ItemBuilder builder = new ItemBuilder(itemStack);

		handleTeamColours(builder);

		builder.setTitle(getItemName(itemStack));
		builder.addLore("");

		if (shouldScale(item))
		{
			builder.addLore(
					"§eShift-Click to buy 4x Stack!",
					"Cost: " + _resource.getChatColor() + getCost(item, 1) + " " + _resource.getName() + "s",
					"",
					result.getColour() + result.getFeedback()
			);
		}
		else
		{
			builder.addLore(
					"Cost: " + _resource.getChatColor() + getCost(item, 1) + " " + _resource.getName() + "s",
					"",
					result.getColour() + result.getFeedback()
			);
		}

		return builder.build();
	}

	private org.bukkit.DyeColor getDyeColor(ChatColor chatColor)
	{
		if (chatColor == ChatColor.WHITE) return org.bukkit.DyeColor.WHITE;
		if (chatColor == ChatColor.GOLD) return org.bukkit.DyeColor.ORANGE;
		if (chatColor == ChatColor.LIGHT_PURPLE) return org.bukkit.DyeColor.PINK;
		if (chatColor == ChatColor.AQUA) return org.bukkit.DyeColor.LIGHT_BLUE;
		if (chatColor == ChatColor.YELLOW) return org.bukkit.DyeColor.YELLOW;
		if (chatColor == ChatColor.GREEN) return org.bukkit.DyeColor.LIME;
		if (chatColor == ChatColor.DARK_GRAY) return org.bukkit.DyeColor.GRAY;
		if (chatColor == ChatColor.GRAY) return org.bukkit.DyeColor.LIGHT_GRAY;
		if (chatColor == ChatColor.DARK_AQUA) return org.bukkit.DyeColor.CYAN;
		if (chatColor == ChatColor.DARK_PURPLE) return org.bukkit.DyeColor.PURPLE;
		if (chatColor == ChatColor.BLUE || chatColor == ChatColor.DARK_BLUE) return org.bukkit.DyeColor.BLUE;
		if (chatColor == ChatColor.DARK_GREEN) return org.bukkit.DyeColor.GREEN;
		if (chatColor == ChatColor.RED || chatColor == ChatColor.DARK_RED) return org.bukkit.DyeColor.RED;
		return org.bukkit.DyeColor.WHITE;
	}

	private void handleTeamColours(ItemBuilder builder)
	{
		Material material = builder.getType();
		String name = material.name();
 
		if (name.contains("WOOL") || name.contains("TERRACOTTA") || name.contains("CLAY") || name.contains("GLASS") || name.contains("DYE") || name.contains("INK_SACK"))
		{
			org.bukkit.DyeColor dyeColor = getDyeColor(_team.GetColor());
			String newMatName = dyeColor.name() + "_";
			if (name.contains("WOOL")) newMatName += "WOOL";
			else if (name.contains("TERRACOTTA") || name.contains("CLAY")) newMatName += "TERRACOTTA";
			else if (name.contains("GLASS")) newMatName += "STAINED_GLASS";
			else if (name.contains("DYE") || name.contains("INK_SACK")) newMatName += "DYE";
			
			Material newMat = Material.getMaterial(newMatName);
			if (newMat != null)
			{
				builder.setType(newMat);
			}
		}
	}

	private String getItemName(ItemStack itemStack)
	{
		if (itemStack.getType().name().contains("TERRACOTTA") || itemStack.getType().name().contains("CLAY"))
		{
			return C.mItem + "Terracotta";
		}
		if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName())
		{
			return itemStack.getItemMeta().getDisplayName();
		}

		return ItemStackFactory.Instance.GetName(itemStack, true);
	}

	private int getCost(BedwarsItem item, int multiplier)
	{
		return shouldScale(item) ? item.getCost() * multiplier : item.getCost();
	}

	private boolean shouldScale(BedwarsItem item)
	{
		return item.getItemType().isMultiBuy();
	}

	protected enum BedShopResult
	{

		SUCCESSFUL(ChatColor.GREEN, "Click to purchase!"),
		NOT_ENOUGH_RESOURCES(ChatColor.RED, "You do not have enough resources!"),
		ALREADY_OWNED(ChatColor.RED, "You already have purchased this item."),
		ONLY_ONE(ChatColor.RED, "Your team already owns this upgrade."),
		MAX_TIER(ChatColor.RED, "Your team has already unlocked the maximum tier.");

		private final ChatColor _colour;
		private final String _feedback;

		BedShopResult(ChatColor colour, String feedback)
		{
			_colour = colour;
			_feedback = feedback;
		}

		public ChatColor getColour()
		{
			return _colour;
		}

		public String getFeedback()
		{
			return _feedback;
		}
	}

	private class BedShopButton implements IButton
	{

		private final BedwarsItem _item;

		BedShopButton(BedwarsItem item)
		{
			_item = item;
		}

		@Override
		public void onClick(Player player, ClickType clickType)
		{
			if (!Recharge.Instance.use(player, "Buy Item", 250, false, false))
			{
				return;
			}

			int multiplier = 1;
			if (_item.getItemType() == BedwarsShopItemType.BLOCK && clickType.isShiftClick())
			{
				multiplier = 4;
			}

			BedShopResult result = getResultPurchase(_item, multiplier);

			if (result != BedShopResult.SUCCESSFUL)
			{
				player.sendMessage(F.main("Game", result.getFeedback()));
				playDenySound(player);
				return;
			}

			BedwarsShopItemType itemType = _item.getItemType();
			ItemStack resource = _resource.getItemStack();
			ItemStack itemStack = _item.getItemStack();
			ItemBuilder give = new ItemBuilder(itemStack);

			if (!shouldScale(_item))
			{
				give.setUnbreakable(true);
			}
			else
			{
				give.setAmount(itemStack.getAmount() * multiplier);
			}

			UtilInv.remove(player, resource.getType(), resource.getData().getData(), getCost(_item, multiplier));
			handleTeamColours(give);

			ItemStack giveItem = give.build();
			PlayerInventory inventory = player.getInventory();

			if (itemType.getRemoveOnPurchase() != null)
			{
				for (int i = 0; i < inventory.getSize(); i++)
				{
					if (itemType.getRemoveOnPurchase().matches(inventory.getItem(i)))
					{
						inventory.setItem(i, null);
					}
				}
			}

			switch (itemType)
			{
				case HELMET:
					inventory.setHelmet(giveItem);
					break;
				case CHESTPLATE:
					inventory.setChestplate(giveItem);
					break;
				case LEGGINGS:
					inventory.setLeggings(giveItem);
					break;
				case BOOTS:
					inventory.setBoots(giveItem);
					break;
				default:
					if (itemType.isItem())
					{
						UtilInv.insert(player, giveItem);
					}
					break;
			}

			Set<BedwarsItem> ownedItems = _game.getBedwarsShopModule().getOwnedItems(player);

			if (!itemType.isMultiBuy())
			{
				ownedItems.removeIf(item -> item.getItemType().equals(itemType));
			}

			if (itemType.isOnePerTeam())
			{
				_game.getBedwarsShopModule().getOwnedItems(_team).add(_item);
			}

			ownedItems.add(_item);

			player.sendMessage(F.main("Game", "You purchased " + F.name(getItemName(giveItem))) + ".");
			playAcceptSound(player);
			getShop().getPageMap().values().forEach(page ->
			{
				if (page.getName().equals(getName()))
				{
					page.refresh();
				}
			});
		}
	}
}
