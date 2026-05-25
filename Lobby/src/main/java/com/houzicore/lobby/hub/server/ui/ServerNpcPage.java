package com.houzicore.lobby.hub.server.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.SkullMeta;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.core.achievement.AchievementCategory;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.game.GameDisplay;
import com.houzicore.shared.core.lang.LangManager;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.lobby.hub.server.ServerInfo;
import com.houzicore.lobby.hub.server.ServerManager;
import com.houzicore.lobby.hub.server.ServerSorter;
import com.houzicore.lobby.hub.server.ServerStatusType;
import com.houzicore.lobby.hub.server.ui.button.JoinServerButton;

public class ServerNpcPage extends ShopPageBase<ServerManager, ServerNpcShop> implements IServerPage
{
	private String _serverNpcKey;
	private boolean _onMainPage = true;
	private IButton _goBackButton;

	public ServerNpcPage(ServerManager plugin, ServerNpcShop shop, CoreClientManager clientManager,	DonationManager donationManager, String name, Player player, String serverNpcKey)
	{
		this(plugin, shop, clientManager, donationManager, name, player, serverNpcKey, null);
	}
	
	public ServerNpcPage(ServerManager plugin, ServerNpcShop shop, CoreClientManager clientManager,	DonationManager donationManager, String name, Player player, String serverNpcKey, IButton goBackButton)
	{
		super(plugin, shop, clientManager, donationManager, com.houzicore.shared.common.util.UtilText.toSmallCaps(name), player, 54);
		
		_serverNpcKey = serverNpcKey;
		_goBackButton = goBackButton;

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		int essence = 0;
		if (getDonationManager() != null && getDonationManager().Get(getPlayer().getName()) != null) {
			essence = getDonationManager().Get(getPlayer().getName()).GetEssence();
		}
		org.bukkit.inventory.ItemStack glass = com.houzicore.shared.core.itemstack.ItemStackFactory.Instance.CreateStack(
			Material.LIGHT_BLUE_STAINED_GLASS_PANE, (byte) 0, 1,
			"§bEssence: §a" + essence
		);
		for (int i = 0; i < getSize(); i++) {
			getInventory().setItem(i, glass);
		}

		List<ServerInfo> serverList = new ArrayList<ServerInfo>(getPlugin().GetServerList(_serverNpcKey));
		int slotsNeeded = 1;

		if (serverList.size() > 0)
		{
			slotsNeeded = getPlugin().GetRequiredSlots(getPlayer(), serverList.get(0).ServerType);
		}

		try
		{
			Collections.sort(serverList, new ServerSorter(slotsNeeded));
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}


		if (_onMainPage)
		{
			buildAvailableServerPage(serverList, slotsNeeded);
		}
		else
		{
			buildInProgressServerPage(serverList, slotsNeeded);
		}
	}

	private void showClock(long milliseconds, boolean beta)
	{
		int seconds = (int) (milliseconds / 1000);
		String timeLeft = UtilTime.convertString(milliseconds, 0, UtilTime.TimeUnit.FIT);

		byte data = (byte) (milliseconds - (seconds * 1000) > 500 ? 15 : 14);

		ShopItem item = new ShopItem(Material.CLOCK, (byte)0, C.cYellow + C.Bold + LangManager.get().get(getPlayer(), "server.npc.queue_timer"), null, new String[] {
				ChatColor.RESET + "",
				colorLine(LangManager.get().get(getPlayer(), "server.npc.queue_time_remaining", timeLeft)),
				ChatColor.RESET + "",
				colorLine(LangManager.get().get(getPlayer(), "server.npc.queue_wait")),
		}, seconds, false, false);

		addItem(22, item);
	}

	private ShopItem buildShopItem(ServerInfo serverInfo, int slotsNeeded)
	{
		boolean ownsUltraPackage = getDonationManager().Get(getPlayer().getName()).OwnsUnknownPackage(serverInfo.ServerType + " ULTRA") || getClient().GetRank().Has(Rank.WARRIOR);
		Material status = Material.RED_STAINED_GLASS_PANE;
		List<String> lore = new ArrayList<String>();

		String inProgress = (serverInfo.Game == null || serverInfo.ServerType.equalsIgnoreCase("Competitive")) ? loreLine("server.npc.lore.in_progress") : loreLine("server.npc.lore.click_spectate");
		String wait = (serverInfo.Game == null || serverInfo.ServerType.equalsIgnoreCase("Competitive")) ? null : loreLine("server.npc.lore.wait_next");

		if (isStarting(serverInfo) && (serverInfo.MaxPlayers - serverInfo.CurrentPlayers) >= slotsNeeded)
			status = Material.LIME_STAINED_GLASS_PANE;
		else if (isInProgress(serverInfo))
			status = Material.YELLOW_STAINED_GLASS_PANE;

		lore.add(ChatColor.RESET + "");

		if (serverInfo.Game != null)
			lore.add(colorLine(LangManager.get().get(getPlayer(), "server.npc.lore.game", serverInfo.Game)));

		if (serverInfo.Map != null && !serverInfo.ServerType.equalsIgnoreCase("Competitive"))
				lore.add(colorLine(LangManager.get().get(getPlayer(), "server.npc.lore.map", serverInfo.Map)));

		lore.add(colorLine(LangManager.get().get(getPlayer(), "server.npc.lore.players", serverInfo.CurrentPlayers, serverInfo.MaxPlayers)));
		lore.add(ChatColor.RESET + "");
		lore.add(ChatColor.RESET + (serverInfo.MOTD != null ? serverInfo.MOTD : ""));

		if (serverInfo.Name.contains("T_") && !ownsUltraPackage)
		{
			lore.add(loreLine("server.npc.lore.beta_ultra"));
		}
		else
		{
			if (serverInfo.Status == ServerStatusType.IN_PROGRESS)
			{
				if (serverInfo.MOTD != null && serverInfo.MOTD.contains("Restarting"))
				{
					status = Material.GRAY_STAINED_GLASS_PANE;
					lore.add(loreLine("server.npc.lore.restarting"));
				}
				else
				{
					if (serverInfo.Game != null && serverInfo.Game.equalsIgnoreCase("Survival Primal Game"))
					{
						lore.add(loreLine("server.npc.lore.survival_locked.1"));
						lore.add(loreLine("server.npc.lore.survival_locked.2"));
					}
					else
					{
						if (!ownsUltraPackage)
						{
							lore.add(loreLine("server.npc.lore.get_ultra"));
						}
						else
						{
							lore.add(inProgress);
							if (wait != null)
								lore.add(wait);
						}
					}
				}
			}
			else
			{
				if (serverInfo.CurrentPlayers >= serverInfo.MaxPlayers && !ownsUltraPackage)
				{
					lore.add(loreLine("server.npc.lore.get_ultra"));
				}
				else if (!serverInfo.MOTD.contains("Open in"))
				{
					lore.add(loreLine("server.npc.lore.click_join"));
				}
			}
		}

		return new ShopItem(status, ChatColor.RESET + C.cGreen + C.Line + C.Bold + LangManager.get().get(getPlayer(), "server.npc.server_label", serverInfo.Name.split("-")[1]), lore.toArray(new String[lore.size()]), serverInfo.CurrentPlayers, false);
	}

	private void buildAvailableServerPage(List<ServerInfo> serverList, int slotsNeeded)
	{
		int[] innerSlots = {10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33,34, 37,38,39,40,41,42,43};
		int maxServers = innerSlots.length;
		int greenCount = 0;
		int yellowCount = 0;
		int maxFull = 10;
		boolean showGreen = true;

		boolean beta = serverList.size() > 0 && serverList.get(0).Name.contains("BETA");
		boolean tournament = serverList.size() > 0 && serverList.get(0).Name.contains("T_");
		boolean privateServer = serverList.size() > 0 && serverList.get(0).ServerType.equals("Player");
		boolean ownsUltraPackage = getClient().GetRank().Has(Rank.WARRIOR) || (serverList.size() > 0 && getDonationManager().Get(getPlayer().getName()).OwnsUnknownPackage(serverList.get(0).ServerType + " ULTRA"));

		long portalTime = getPlugin().getMillisecondsUntilPortal(getPlayer(), beta);
		if (portalTime > 0)
		{
			showClock(portalTime, beta);
			showGreen = false;
		}
		else if (tournament && !ownsUltraPackage)
		{
			ShopItem item = new ShopItem(Material.REDSTONE_BLOCK, (byte)0,  ChatColor.RESET + C.Bold + LangManager.get().get(getPlayer(), "server.tournament.title"), null, new String[] {
					ChatColor.RESET + "",
					ChatColor.RESET + C.cAqua + LangManager.get().get(getPlayer(), "server.tournament.desc.1"),
					ChatColor.RESET + C.cAqua + LangManager.get().get(getPlayer(), "server.tournament.desc.2"),
					ChatColor.RESET + "",
					ChatColor.RESET + "",
					ChatColor.RESET + C.cGreen + LangManager.get().get(getPlayer(), "server.tournament.link")
			}, 1, false, false);
	
			addItem(22, item);

			return;
		}
		else if (privateServer)
		{
			int staffSlot = 0;
			int slotIndex = 0;
			for (ServerInfo serverInfo : serverList)
			{
				if (serverInfo.MOTD.contains("Private"))
					continue;

				if (serverInfo.MaxPlayers - serverInfo.CurrentPlayers <= 0)
					continue;

				if (serverInfo.HostedByStaff && staffSlot < 9)
				{
					addButton(staffSlot, getPrivateItem(serverInfo), new JoinServerButton(this, serverInfo));
					staffSlot++;
				}
				else
				{
					if (slotIndex >= maxServers)
						continue;

					addButton(innerSlots[slotIndex], getPrivateItem(serverInfo), new JoinServerButton(this, serverInfo));
					slotIndex++;
				}
			}
			return;
		}
		
		int fullCount = 0;

		for (ServerInfo serverInfo : serverList)
		{
			if (isStarting(serverInfo) && hasEnoughSlots(serverInfo, slotsNeeded) && greenCount < maxServers)
			{
				if (showGreen)
				{
					boolean full = serverInfo.MaxPlayers - serverInfo.CurrentPlayers <= 0;
					
					if (full && fullCount >= maxFull)
						continue;
						
					ShopItem shopItem = buildShopItem(serverInfo, slotsNeeded);

					int currentSlot = innerSlots[greenCount];

					if (serverInfo.MOTD.contains("Open in"))
						setItem(currentSlot, shopItem);
					else
						addButton(currentSlot, shopItem, new JoinServerButton(this, serverInfo));
					
					greenCount++;
					
					if (full)
						fullCount++;
				}
			}
			else if (isInProgress(serverInfo))
			{
				yellowCount++;
			}
		}

		addButton(50, new ShopItem(Material.YELLOW_STAINED_GLASS_PANE, C.cAqua + LangManager.get().get(getPlayer(), "server.npc.in_progress_count", yellowCount), new String[]{loreLine("server.npc.lore.click_spectate")}, yellowCount > 64 ? 64 : yellowCount, false), new IButton()
		{
			@Override
			public void onClick(Player player, ClickType clickType)
			{
				_onMainPage = false;
				refresh();
			}
		});

		// Go Back Button
		IButton backButtonAction = _goBackButton;
		if (backButtonAction == null)
		{
			backButtonAction = new IButton()
			{
				@Override
				public void onClick(Player player, ClickType clickType)
				{
					getShop().openPageForPlayer(player, new ServerNpcSelectionPage(
						getPlugin(), getShop(), getClientManager(), getDonationManager(),
						getName(), player, _serverNpcKey));
				}
			};
		}
		addButton(49, new ShopItem(Material.RED_BED, C.cGray + " \u21FD " + LangManager.get().get(getPlayer(), "server.lobby.go_back"), new String[]{}, 1, false), backButtonAction);
	}

	private ShopItem getPrivateItem(ServerInfo serverInfo)
	{
		String hostName = serverInfo.Name.substring(0, serverInfo.Name.indexOf('-'));
		String server = ChatColor.GREEN + C.Bold + serverInfo.Name;
		String host = colorLine(LangManager.get().get(getPlayer(), "server.npc.private_host", hostName));
		Material material = Material.PLAYER_HEAD;
		byte data = (byte) 3;

		ArrayList<String> lore = new ArrayList<String>();
		lore.add(host);
		lore.add(" ");
		lore.add(colorLine(LangManager.get().get(getPlayer(), "server.npc.lore.players", serverInfo.CurrentPlayers, serverInfo.MaxPlayers)));
		lore.add(" ");

		if (serverInfo.Game != null)
		{
			lore.add(colorLine(LangManager.get().get(getPlayer(), "server.npc.lore.game", serverInfo.Game)));
			GameDisplay display = GameDisplay.matchName(serverInfo.Game);
			if (display != null)
			{
				material = display.getMaterial();
				data = display.getMaterialData();
			}
		}

		if (serverInfo.Map != null)
			lore.add(colorLine(LangManager.get().get(getPlayer(), "server.npc.lore.map", serverInfo.Map)));

		if (serverInfo.HostedByStaff)
		{
			lore.add(" ");
			lore.add(ChatColor.RESET + C.cGreen + LangManager.get().get(getPlayer(), "server.npc.private_staff"));
		}

		ShopItem shopItem = new ShopItem(material, data, server, lore.toArray(new String[0]), 1, false, false);
		if (material == Material.PLAYER_HEAD)
		{
			SkullMeta meta = (SkullMeta) shopItem.getItemMeta();
			meta.setOwner(hostName);
			shopItem.setItemMeta(meta);
		}

		return shopItem;
	}

	private boolean isStarting(ServerInfo serverInfo)
	{
		return serverInfo.Status == ServerStatusType.STARTING;
	}

	private boolean isInProgress(ServerInfo serverInfo)
	{
		return serverInfo.Status == ServerStatusType.IN_PROGRESS;
	}

	private boolean hasEnoughSlots(ServerInfo serverInfo, int slotsNeeded)
	{
		return (serverInfo.MaxPlayers - serverInfo.CurrentPlayers) >= slotsNeeded;
	}

	private void buildInProgressServerPage(List<ServerInfo> serverList, int slotsNeeded)
	{
		int[] innerSlots = {10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33,34, 37,38,39,40,41,42,43};
		int slotIndex = 0;

		for (ServerInfo serverInfo : serverList)
		{
			if (isInProgress(serverInfo) && slotIndex < innerSlots.length)
			{
				ShopItem shopItem = buildShopItem(serverInfo, slotsNeeded);

				addButton(innerSlots[slotIndex], shopItem, new JoinServerButton(this, serverInfo));

				slotIndex++;
			}
		}

		addButton(49, new ShopItem(Material.RED_BED, C.cGray + " \u21FD " + LangManager.get().get(getPlayer(), "server.lobby.go_back"), new String[]{}, 1, false), new IButton()
		{
			@Override
			public void onClick(Player player, ClickType clickType)
			{
				_onMainPage = true;
				refresh();
			}
		});
	}

	public void Update()
	{
		getButtonMap().clear();
		buildPage();
	}

	public void SelectServer(Player player, ServerInfo serverInfo)
	{		
		int slots = getPlugin().GetRequiredSlots(player, serverInfo.ServerType);
		
		if (serverInfo.MaxPlayers - serverInfo.CurrentPlayers < slots && !(getDonationManager().Get(getPlayer().getName()).OwnsUnknownPackage(serverInfo.ServerType + " ULTRA") || getClient().GetRank().Has(Rank.WARRIOR)))
		{
			playDenySound(player);
			return;
		}
		
		getPlugin().SelectServer(player, serverInfo);
	}

	private String loreLine(String key, Object... args)
	{
		return ChatColor.RESET + C.Line + LangManager.get().get(getPlayer(), key, args);
	}

	private String colorLine(String text)
	{
		return ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', text);
	}
}
