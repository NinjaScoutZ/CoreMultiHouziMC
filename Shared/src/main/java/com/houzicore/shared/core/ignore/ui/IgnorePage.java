package com.houzicore.shared.core.ignore.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.ignore.IgnoreManager;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import org.bukkit.event.inventory.ClickType;

public class IgnorePage extends ShopPageBase<IgnoreManager, IgnoreShop> {

	public IgnorePage(IgnoreManager plugin, IgnoreShop shop, CoreClientManager clientManager, Player player) {
		super(plugin, shop, clientManager, null, "Ignore List", player, 54);
		buildPage();
	}

	@Override
	protected void buildPage() {
		List<String> ignoredPlayers = new ArrayList<>(getPlugin().Get(getPlayer()).getIgnored());
		Collections.sort(ignoredPlayers);

		int size = Math.max(9, ((ignoredPlayers.size() / 9) + 1) * 9);
		if (size > 54) size = 54;

		int slot = 0;
		for (final String ignoredName : ignoredPlayers) {
			if (slot >= size) break;

			ShopItem item = buildIgnoreHead(ignoredName);
			IButton button = (player, clickType) -> {
				if (clickType.isLeftClick() || clickType.isRightClick()) {
					player.closeInventory();
					getPlugin().removeIgnore(player, ignoredName);
				}
			};

			addButton(slot++, item, button);
		}

		ShopItem filler = new ShopItem(Material.RED_STAINED_GLASS_PANE, " ", new String[0], 1, false);
		for (int i = slot; i < size; i++) {
			addItem(i, filler);
		}
	}

	@SuppressWarnings("deprecation")
	private ShopItem buildIgnoreHead(String name) {
		ItemStack head = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta meta = (SkullMeta) head.getItemMeta();
		meta.setOwner(name);

		List<String> lore = new ArrayList<>();
		meta.setDisplayName(ChatColor.RED + name);
		lore.add(ChatColor.GRAY + "Ignored Player");
		lore.add("");
		lore.add(ChatColor.RED + "Click to Unignore");

		meta.setLore(lore);
		head.setItemMeta(meta);

		return new ShopItem(head, meta.getDisplayName(), "", 1, false, false);
	}
}
