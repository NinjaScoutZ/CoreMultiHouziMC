package com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.ui;

import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import com.houzicore.shared.common.util.C;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.BedwarsNetherItem;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.common.util.UtilText;
import com.houzicore.shared.core.itemstack.ItemBuilder;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.core.shop.item.IButton;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.BedwarsItem;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.BedwarsResource;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.shop.BedwarsTeamItem;
import com.houzicore.arcade.nautilus.game.arcade.game.games.bedwars.team.BedwarsTeam;

public class BedwarsResourceStarPage extends BedwarsResourcePage
{

	private static final ItemStack CLOSE_ITEM = new ItemBuilder(Material.BARRIER)
			.setTitle(C.cRed + C.Bold + "Close")
			.build();

	private final BedwarsTeam _bedTeam;

	public BedwarsResourceStarPage(ArcadeManager plugin, BedwarsResourceShop shop, Player player, List<BedwarsItem> items)
	{
		super(plugin, shop, player, 45, BedwarsResource.STAR, items);

		_bedTeam = _game.getBedwarsTeamModule().getBedwarsTeam(_game.GetTeam(player));
	}

	@Override
	protected void buildPage()
	{
		// 1. Build borders and separators
		// Top row border (slots 0-7)
		for (int i = 0; i <= 7; i++)
		{
			addButton(i, new ItemBuilder(Material.BLUE_STAINED_GLASS_PANE).setTitle(" ").build(), (player, clickType) -> {});
		}
		// Close button at slot 8
		addButton(8, CLOSE_ITEM, (player, clickType) -> player.closeInventory());

		// Side borders and separators
		addButton(9, new ItemBuilder(Material.BLUE_STAINED_GLASS_PANE).setTitle(" ").build(), (player, clickType) -> {});
		addButton(13, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setTitle(" ").build(), (player, clickType) -> {});
		addButton(17, new ItemBuilder(Material.BLUE_STAINED_GLASS_PANE).setTitle(" ").build(), (player, clickType) -> {});

		addButton(18, new ItemBuilder(Material.BLUE_STAINED_GLASS_PANE).setTitle(" ").build(), (player, clickType) -> {});
		addButton(22, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setTitle(" ").build(), (player, clickType) -> {});
		addButton(26, new ItemBuilder(Material.BLUE_STAINED_GLASS_PANE).setTitle(" ").build(), (player, clickType) -> {});

		addButton(27, new ItemBuilder(Material.BLUE_STAINED_GLASS_PANE).setTitle(" ").build(), (player, clickType) -> {});
		addButton(31, new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setTitle(" ").build(), (player, clickType) -> {});
		addButton(35, new ItemBuilder(Material.BLUE_STAINED_GLASS_PANE).setTitle(" ").build(), (player, clickType) -> {});

		// Bottom row border (slots 36-39, 41-44)
		for (int i = 36; i <= 44; i++)
		{
			if (i == 40) continue;
			addButton(i, new ItemBuilder(Material.BLUE_STAINED_GLASS_PANE).setTitle(" ").build(), (player, clickType) -> {});
		}

		// Diamond Resource Count Display at Slot 40
		int ownedResourceCount = UtilInv.countItems(getPlayer(), _resource.getItemStack().getType(), (byte) 0);
		addButton(40, new ItemBuilder(_resource.getItemStack().getType())
				.setTitle(_resource.getChatColor() + C.Bold + "You have: " + ownedResourceCount + " " + _resource.getName() + "s")
				.build(), (player, clickType) -> {});

		// 2. Set up upgrades
		setupUpgradeSlot(BedwarsNetherItem.PROTECTION, 10, 11, 12);
		setupUpgradeSlot(BedwarsNetherItem.HASTE, 19, 20, 21);
		setupUpgradeSlot(BedwarsNetherItem.SHARPNESS, 28, 29, 30);

		setupUpgradeSlot(BedwarsNetherItem.POWER, 14, 15, 16);
		setupUpgradeSlot(BedwarsNetherItem.RESOURCE, 23, 24, 25);
		setupUpgradeSlot(BedwarsNetherItem.REGENERATION, 32, 33, 34);
	}

	private void setupUpgradeSlot(BedwarsNetherItem item, int itemSlot, int tier1Slot, int tier2Slot)
	{
		int level = _bedTeam.getUpgrades().get(item);
		BedShopResult result = getResultPurchase(item, 1);

		// Prepare main item stack
		ItemStack mainItem = prepareItem(item, result);
		addButton(itemSlot, mainItem, new BedwarsTeamItemButton(item));

		// Prepare Glass Panes for Tiers
		ItemStack glass1 = prepareGlassPane(item, 1, level, result);
		addButton(tier1Slot, glass1, new BedwarsTeamItemGlassButton(item, 1));

		ItemStack glass2 = prepareGlassPane(item, 2, level, result);
		addButton(tier2Slot, glass2, new BedwarsTeamItemGlassButton(item, 2));
	}

	private ItemStack prepareGlassPane(BedwarsNetherItem item, int tier, int currentLevel, BedShopResult result)
	{
		ItemBuilder builder;
		if (currentLevel >= tier)
		{
			builder = new ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
					.setTitle("§a" + item.getName() + " " + UtilText.toRomanNumeral(tier) + " - Unlocked");
			builder.addLore("");
			for (String descLine : item.getDescription(tier - 1))
			{
				builder.addLore(descLine);
			}
			builder.addLore("", "§aAlready Unlocked");
		}
		else if (currentLevel == tier - 1)
		{
			builder = new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
					.setTitle("§c" + item.getName() + " " + UtilText.toRomanNumeral(tier) + " - Click to Buy");
			builder.addLore("");
			for (String descLine : item.getDescription(tier - 1))
			{
				builder.addLore(descLine);
			}
			builder.addLore(
					"",
					"Cost: " + _resource.getChatColor() + item.getLevels()[tier - 1].getRight() + " " + _resource.getName() + "s",
					"",
					result.getColour() + result.getFeedback()
			);
		}
		else
		{
			builder = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
					.setTitle("§8" + item.getName() + " " + UtilText.toRomanNumeral(tier) + " - Locked");
			builder.addLore("", "§7You must unlock the previous tier first.");
		}
		return builder.build();
	}

	private void buyUpgrade(Player player, BedwarsNetherItem item)
	{
		if (!Recharge.Instance.use(player, "Buy Team Upgrade", 250, false, false))
		{
			return;
		}

		BedShopResult result = getResultPurchase(item, 1);

		if (result != BedShopResult.SUCCESSFUL)
		{
			player.sendMessage(F.main("Game", result.getFeedback()));
			playDenySound(player);
			return;
		}

		int level = _bedTeam.getUpgrades().get(item);
		ItemStack resource = _resource.getItemStack();
		UtilInv.remove(player, resource.getType(), resource.getData().getData(), item.getLevels()[level].getRight());

		int newLevel = level + 1;
		String name = F.name(item.getName() + " " + UtilText.toRomanNumeral(newLevel));
		String message = F.main("Game", _team.GetColor() + getPlayer().getName() + C.cGray + " purchased the " + name + " team upgrade.");

		for (Player other : _team.GetPlayers(false))
		{
			other.playSound(other.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 0.6F);
			other.sendMessage(message);
		}

		_bedTeam.getUpgrades().put(item, newLevel);

		playAcceptSound(player);
		getShop().getPageMap().values().forEach(page ->
		{
			if (page.getName().equals(getName()))
			{
				page.refresh();
			}
		});
	}

	@Override
	BedShopResult getResultPurchase(BedwarsItem item, int multiplier)
	{
		BedwarsTeamItem teamItem = (BedwarsTeamItem) item;
		int level = _bedTeam.getUpgrades().get(item);
		ItemStack itemStack = _resource.getItemStack();

		if (level == teamItem.getLevels().length)
		{
			return BedShopResult.MAX_TIER;
		}
		else if (!UtilInv.contains(getPlayer(), itemStack.getType(), (byte) 0, teamItem.getLevels()[level].getRight()))
		{
			return BedShopResult.NOT_ENOUGH_RESOURCES;
		}

		return BedShopResult.SUCCESSFUL;
	}

	@Override
	ItemStack prepareItem(BedwarsItem item, BedShopResult result)
	{
		BedwarsTeamItem teamItem = (BedwarsTeamItem) item;
		ItemBuilder builder = new ItemBuilder(item.getItemStack());
		int level = _bedTeam.getUpgrades().get(item);
		String name = C.mItem + teamItem.getName();
		boolean maxTier = result == BedShopResult.MAX_TIER;

		if (!maxTier)
		{
			name += " " + UtilText.toRomanNumeral(level + 1);
		}

		builder.setTitle(name);

		if (maxTier)
		{
			builder.addLore("", C.cRed + result.getColour() + result.getFeedback());
		}
		else
		{
			builder.addLore("");
			builder.addLore(teamItem.getDescription(level));

			builder.addLore(
					"",
					"Cost: " + _resource.getChatColor() + teamItem.getLevels()[level].getRight() + " " + _resource.getName() + "s",
					"",
					result.getColour() + result.getFeedback()
			);

		}

		return builder.build();
	}

	private class BedwarsTeamItemButton implements IButton
	{
		private final BedwarsNetherItem _item;

		BedwarsTeamItemButton(BedwarsNetherItem item)
		{
			_item = item;
		}

		@Override
		public void onClick(Player player, ClickType clickType)
		{
			buyUpgrade(player, _item);
		}
	}

	private class BedwarsTeamItemGlassButton implements IButton
	{
		private final BedwarsNetherItem _item;
		private final int _tier;

		BedwarsTeamItemGlassButton(BedwarsNetherItem item, int tier)
		{
			_item = item;
			_tier = tier;
		}

		@Override
		public void onClick(Player player, ClickType clickType)
		{
			int currentLevel = _bedTeam.getUpgrades().get(_item);
			if (currentLevel >= _tier)
			{
				player.sendMessage(F.main("Game", "Your team has already unlocked this tier."));
				playDenySound(player);
				return;
			}
			if (currentLevel < _tier - 1)
			{
				player.sendMessage(F.main("Game", "You must unlock the previous tier first."));
				playDenySound(player);
				return;
			}

			buyUpgrade(player, _item);
		}
	}
}
