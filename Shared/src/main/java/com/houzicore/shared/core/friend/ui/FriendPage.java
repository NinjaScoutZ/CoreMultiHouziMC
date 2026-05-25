package com.houzicore.shared.core.friend.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.friend.FriendManager;
import com.houzicore.shared.core.friend.FriendSorter;
import com.houzicore.shared.core.friend.FriendStatusType;
import com.houzicore.shared.core.friend.data.FriendStatus;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import org.bukkit.event.inventory.ClickType;

public class FriendPage extends ShopPageBase<FriendManager, FriendShop> {

	private static final FriendSorter SORTER = new FriendSorter();

	public FriendPage(FriendManager plugin, FriendShop shop, CoreClientManager clientManager, Player player) {
		super(plugin, shop, clientManager, null, "Friends", player, 54);
		buildPage();
	}

	@Override
	protected void buildPage() {
		List<FriendStatus> friends = new ArrayList<>(getPlugin().Get(getPlayer()).getFriends());
		Collections.sort(friends, SORTER);

		friends.removeIf(f -> f.Status == FriendStatusType.Blocked || f.Status == FriendStatusType.Denied);

		int size = Math.max(9, ((friends.size() / 9) + 1) * 9);
		if (size > 54) size = 54;

		int slot = 0;
		for (final FriendStatus friend : friends) {
			if (slot >= size) break;

			ShopItem item = buildFriendHead(friend);
			IButton button = (player, clickType) -> {
				if (clickType.isShiftClick() && clickType.isLeftClick() && friend.Status == FriendStatusType.Accepted) {
					player.closeInventory();
					getPlugin().updateFavorite(player, friend.Name, !friend.Favorite);
					player.sendMessage(ChatColor.YELLOW + friend.Name + ChatColor.GRAY + " has been " + (!friend.Favorite ? "added to" : "removed from") + " your favorites.");
				} else if (clickType.isLeftClick()) {
					if (friend.Status == FriendStatusType.Accepted && friend.Online) {
						player.closeInventory();
						getPlugin().getPortal().sendPlayerToServer(player, friend.ServerName, true);
					} else if (friend.Status == FriendStatusType.Pending) {
						player.closeInventory();
						getPlugin().addFriend(player, friend.Name);
					}
				} else if (clickType.isRightClick()) {
					player.closeInventory();
					getPlugin().removeFriend(player, friend.Name);
				}
			};

			addButton(slot++, item, button);
		}

		ShopItem filler = new ShopItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, " ", new String[0], 1, false);
		for (int i = slot; i < size; i++) {
			addItem(i, filler);
		}
	}

	@SuppressWarnings("deprecation")
	private ShopItem buildFriendHead(FriendStatus friend) {
		ItemStack head = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta meta = (SkullMeta) head.getItemMeta();
		meta.setOwner(friend.Name);

		List<String> lore = new ArrayList<>();
		String star = friend.Favorite ? "★ " : "";

		if (friend.Status == FriendStatusType.Accepted) {
			if (friend.Online) {
				meta.setDisplayName(ChatColor.GREEN + star + friend.Name);
				lore.add(ChatColor.GRAY + "Server: " + ChatColor.GREEN + friend.ServerName);
				lore.add("");
				lore.add(com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "friend.left_click_tp"));
				lore.add(com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "friend.right_click_remove"));
				lore.add(ChatColor.YELLOW + "Shift-Left Click" + ChatColor.GRAY + " to " + (friend.Favorite ? "Unfavorite" : "Favorite"));
			} else {
				meta.setDisplayName(ChatColor.GRAY + star + friend.Name);
				lore.add(com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "friend.status_offline"));
				lore.add("");
				lore.add(com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "friend.right_click_remove"));
				lore.add(ChatColor.YELLOW + "Shift-Left Click" + ChatColor.GRAY + " to " + (friend.Favorite ? "Unfavorite" : "Favorite"));
			}
		} else if (friend.Status == FriendStatusType.Pending) {
			meta.setDisplayName(ChatColor.YELLOW + friend.Name);
			lore.add(com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "friend.requested"));
			lore.add("");
			lore.add(com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "friend.left_click_accept"));
			lore.add(com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "friend.right_click_deny"));
		} else if (friend.Status == FriendStatusType.Sent) {
			meta.setDisplayName(ChatColor.AQUA + friend.Name);
			lore.add(com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "friend.awaiting_response"));
			lore.add("");
			lore.add(com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "friend.right_click_cancel"));
		}

		meta.setLore(lore);
		head.setItemMeta(meta);

		return new ShopItem(head, meta.getDisplayName(), "", 1, false, false);
	}
}
