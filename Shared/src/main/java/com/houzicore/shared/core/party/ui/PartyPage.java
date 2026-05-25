package com.houzicore.shared.core.party.ui;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.party.Party;
import com.houzicore.shared.core.party.PartyManager;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.lang.LangManager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.Bukkit;

public class PartyPage extends ShopPageBase<PartyManager, PartyShop> {

	public PartyPage(PartyManager plugin, PartyShop shop, CoreClientManager clientManager, DonationManager donationManager, Player player) {
		super(plugin, shop, clientManager, donationManager, "Party", player);

		buildPage();
	}

	@Override
	protected void buildPage() {
		Party party = getPlugin().getPartyByPlayer(getPlayer());
		
		if (party == null) {
			// Not in a party
			addEmptyPartyUI();
		} else {
			// In a party
			addActivePartyUI(party);
		}
	}

	private void addEmptyPartyUI() {
		// Glass outline
		for (int i = 0; i < 54; i++) {
			getInventory().setItem(i, ItemStackFactory.Instance.CreateStack(Material.GRAY_STAINED_GLASS_PANE, (byte) 0, 1, " "));
		}

		ShopItem createParty = new ShopItem(Material.DIAMOND, (byte) 0, 
				C.cGreen + C.Bold + LangManager.get().get(getPlayer(), "party.ui_create_name"), 
				new String[] { C.cGray + LangManager.get().get(getPlayer(), "party.ui_create_lore_1"), "", C.cWhite + LangManager.get().get(getPlayer(), "party.ui_create_lore_2") }, 
				1, false, false);
		
		addButton(22, createParty, (clicker, clickType) -> {
			getPlugin().CreateParty(clicker);
			clicker.playSound(clicker.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
			refresh();
		});
	}

	private void addActivePartyUI(Party party) {
		// Border decoration based on whether player is leader
		boolean isLeader = party.getLeaderUuid().equals(getPlayer().getUniqueId());
		Material borderMaterial = isLeader ? Material.YELLOW_STAINED_GLASS_PANE : Material.LIGHT_BLUE_STAINED_GLASS_PANE;
		
		for (int i = 0; i < 54; i++) {
			if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8) {
				getInventory().setItem(i, ItemStackFactory.Instance.CreateStack(borderMaterial, (byte) 0, 1, " "));
			}
		}

		// Show Leader at top center
		ItemStack leaderHead = ItemStackFactory.Instance.CreateStack(Material.PLAYER_HEAD, (byte) 3, 1, C.cGold + C.Bold + "Party Leader: " + party.getLeaderName());
		SkullMeta leaderMeta = (SkullMeta) leaderHead.getItemMeta();
		leaderMeta.setOwningPlayer(Bukkit.getOfflinePlayer(party.getLeaderUuid()));
		leaderHead.setItemMeta(leaderMeta);
		getInventory().setItem(4, leaderHead);
		
		// List members
		int[] memberSlots = new int[] { 20, 21, 22, 23, 24, 29, 30, 31, 32, 33 };
		ArrayList<java.util.UUID> memberUuids = new ArrayList<>(party.getPlayerUuids());
		memberUuids.remove(party.getLeaderUuid()); // Leader is already shown
		
		int memberIndex = 0;
		for (java.util.UUID memberUuid : memberUuids) {
			if (memberIndex >= memberSlots.length) break;
			
            String memberName = party.getName(memberUuid);
			ItemStack memberHead = ItemStackFactory.Instance.CreateStack(Material.PLAYER_HEAD, (byte) 3, 1, C.cYellow + memberName);
			SkullMeta meta = (SkullMeta) memberHead.getItemMeta();
			meta.setOwningPlayer(Bukkit.getOfflinePlayer(memberUuid));
			
			// If we are leader, allow kicking/promoting
			ArrayList<String> lore = new ArrayList<>();
			if (isLeader) {
				lore.add(C.cGray + "Left-Click to " + C.cRed + "Kick");
				lore.add(C.cGray + "Right-Click to " + C.cGreen + "Promote");
			}
			meta.setLore(lore);
			memberHead.setItemMeta(meta);
			
			if (isLeader) {
				String targetToKick = memberName;
                java.util.UUID targetUuid = memberUuid;
				addButton(memberSlots[memberIndex], new ShopItem(memberHead, memberName, memberName, 1, false, false), (clicker, clickType) -> {
                    if (clickType.isLeftClick()) {
					    party.KickParty(targetToKick);
                    } else if (clickType.isRightClick()) {
                        party.PromoteParty(targetUuid);
                    }
					refresh();
				});
			} else {
				getInventory().setItem(memberSlots[memberIndex], memberHead);
			}
			
			memberIndex++;
		}
		
		// Leave Party
		ShopItem leaveParty = new ShopItem(Material.RED_BED, (byte) 0, 
				C.cRed + C.Bold + LangManager.get().get(getPlayer(), "party.ui_leave_name"), 
				new String[] { C.cGray + LangManager.get().get(getPlayer(), "party.ui_leave_lore") }, 
				1, false, false);
		addButton(49, leaveParty, (clicker, clickType) -> {
			party.LeaveParty(clicker);
			clicker.closeInventory();
		});
		
		// Invite players (for Leader only)
		if (isLeader) {
			ShopItem invitePlayers = new ShopItem(Material.EMERALD, (byte) 0, 
					C.cGreen + C.Bold + LangManager.get().get(getPlayer(), "party.ui_invite_name"), 
					new String[] { C.cGray + LangManager.get().get(getPlayer(), "party.ui_invite_lore_1"), LangManager.get().get(getPlayer(), "party.ui_invite_lore_2") }, 
					1, false, false);
			addButton(40, invitePlayers, (clicker, clickType) -> {
                com.houzicore.shared.common.util.UtilPlayer.message(clicker, com.houzicore.shared.common.util.F.main("Party", LangManager.get().get(clicker, "party.ui_invite_prompt")));
                clicker.closeInventory();
			});
		}
	}
}
