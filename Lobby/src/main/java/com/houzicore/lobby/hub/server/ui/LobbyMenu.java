package com.houzicore.lobby.hub.server.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.Material;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.lang.LangManager;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import org.bukkit.event.inventory.ClickType;
import com.houzicore.lobby.hub.server.LobbySorter;
import com.houzicore.lobby.hub.server.ServerInfo;
import com.houzicore.lobby.hub.server.ServerManager;
import com.houzicore.lobby.hub.server.ui.button.JoinServerButton;

public class LobbyMenu extends ShopPageBase<ServerManager, LobbyShop> implements IServerPage
{
	private String _serverGroup;
	
	public LobbyMenu(ServerManager plugin, LobbyShop lobbyShop, CoreClientManager clientManager, DonationManager donationManager, String name, Player player, String serverGroup)
	{
		super(plugin, lobbyShop, clientManager, donationManager, com.houzicore.shared.common.util.UtilText.toSmallCaps(name), player, 54);

		_serverGroup = serverGroup;
		
		buildPage();
	}

	@Override
	protected void buildPage()
	{
		List<ServerInfo> serverList = new ArrayList<ServerInfo>(getPlugin().GetServerList(_serverGroup));

		try
		{
			Collections.sort(serverList, new LobbySorter());
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
		
		int slot = 0;	
		String openFull = ChatColor.RESET + C.Line + LangManager.get().get(getPlayer(), "server.npc.lore.get_ultra");
		String openFullUltra = ChatColor.RESET + C.Line + LangManager.get().get(getPlayer(), "server.npc.lore.click_join");
		
		for (ServerInfo serverInfo : serverList)
		{
			Material status = Material.IRON_BLOCK;
			List<String> lore = new ArrayList<String>();
			
			slot = Integer.parseInt(serverInfo.Name.split("-")[1]) - 1;
			if (slot >= 54)
				continue;
			
			if (serverInfo.Name.equalsIgnoreCase(getPlugin().getStatusManager().getCurrentServerName()))
				status = Material.EMERALD_BLOCK;
			
			lore.add(ChatColor.RESET + "");
			lore.add(ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', LangManager.get().get(getPlayer(), "server.npc.lore.players", serverInfo.CurrentPlayers, serverInfo.MaxPlayers)));
			lore.add(ChatColor.RESET + "");
			
			if (serverInfo.CurrentPlayers >= serverInfo.MaxPlayers)
			{
				if (!getClient().GetRank().Has(Rank.WARRIOR))
					lore.add(openFull);
				else
					lore.add(openFullUltra);
			}
			else
				lore.add(ChatColor.RESET + C.Line + LangManager.get().get(getPlayer(), "server.npc.lore.click_join"));
			
			if (status != Material.EMERALD_BLOCK)
				addButton(slot, new ShopItem(status, ChatColor.UNDERLINE + "" + ChatColor.BOLD + "" + ChatColor.WHITE + LangManager.get().get(getPlayer(), "server.npc.server_label", serverInfo.Name.substring(serverInfo.Name.indexOf('-') + 1)), lore.toArray(new String[lore.size()]), Integer.parseInt(serverInfo.Name.substring(serverInfo.Name.indexOf('-') + 1)), false), new JoinServerButton(this, serverInfo));
			else
				addItem(slot, new ShopItem(status, ChatColor.UNDERLINE + "" + ChatColor.BOLD + "" + ChatColor.WHITE + LangManager.get().get(getPlayer(), "server.npc.server_label", serverInfo.Name.substring(serverInfo.Name.indexOf('-') + 1)), lore.toArray(new String[lore.size()]), Integer.parseInt(serverInfo.Name.substring(serverInfo.Name.indexOf('-') + 1)), false));
		}
		
		while (slot < 54)
		{
			clear(slot);
			slot++;
		}

		// Go Back Button
		addButton(4, new ShopItem(Material.RED_BED, C.cGray + " \u21FD " + LangManager.get().get(getPlayer(), "server.lobby.go_back"), new String[]{}, 1, false), new IButton()
		{
			@Override
			public void onClick(Player player, ClickType clickType)
			{
				getPlugin().getQuickShop().attemptShopOpen(player);
			}
		});
	}

	public void Update()
	{	
		clear();
		getButtonMap().clear();
		buildPage();
	}

	@Override
	public void SelectServer(org.bukkit.entity.Player player, ServerInfo serverInfo)
	{
		int slots = getPlugin().GetRequiredSlots(player, serverInfo.ServerType);
		
		if (serverInfo.MaxPlayers - serverInfo.CurrentPlayers < slots)
		{
			playDenySound(player);
			return;
		}
		
		getPlugin().SelectServer(player, serverInfo);
	}
}
