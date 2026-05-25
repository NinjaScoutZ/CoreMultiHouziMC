package com.houzicore.lobby.hub.server.ui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.lang.LangManager;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.shared.core.shop.item.ShopItem;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.lobby.hub.server.ServerManager;

public class ServerNpcSelectionPage extends ShopPageBase<ServerManager, ServerNpcShop>
{
	private String _serverNpcKey;
	private ServerManager _manager;

	public ServerNpcSelectionPage(ServerManager plugin, ServerNpcShop shop, CoreClientManager clientManager, DonationManager donationManager, String name, Player player, String serverNpcKey)
	{
		super(plugin, shop, clientManager, donationManager, com.houzicore.shared.common.util.UtilText.toSmallCaps(name), player, 45);
		_manager = plugin;
		_serverNpcKey = serverNpcKey;

		buildPage();
	}

	@Override
	protected void buildPage()
	{
		int essence = 0;
		if (getDonationManager() != null && getDonationManager().Get(getPlayer().getName()) != null) {
			essence = getDonationManager().Get(getPlayer().getName()).GetEssence();
		}
		// Blue glass border
		org.bukkit.inventory.ItemStack glass = com.houzicore.shared.core.itemstack.ItemStackFactory.Instance.CreateStack(
			Material.LIGHT_BLUE_STAINED_GLASS_PANE, (byte) 0, 1,
			"§bEssence: §a" + essence
		);
		for (int i = 0; i < getSize(); i++) {
			getInventory().setItem(i, glass);
		}

		// Slot 20: Quick Match
		addButton(20, new ShopItem(Material.EMERALD_BLOCK, (byte)0, ChatColor.RESET + C.cGreen + C.Bold + LangManager.get().get(getPlayer(), "server.selector.quick_match.name"),
				new String[] {
						ChatColor.RESET + "",
						ChatColor.RESET + C.cGray + LangManager.get().get(getPlayer(), "server.selector.quick_match.desc.1"),
						ChatColor.RESET + C.cGray + LangManager.get().get(getPlayer(), "server.selector.quick_match.desc.2"),
						ChatColor.RESET + LangManager.get().get(getPlayer(), "server.selector.quick_match.desc.3", _manager.getQuickMatchRecommendationText(getPlayer(), _serverNpcKey)),
						ChatColor.RESET + LangManager.get().get(getPlayer(), "server.selector.quick_match.desc.4", _manager.getQuickMatchAvailabilityText(getPlayer(), _serverNpcKey)),
						ChatColor.RESET + ""
				}, 1, false, false), new IButton()
		{
			@Override
			public void onClick(Player player, ClickType clickType)
			{
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.85f, 1.15f);
				player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.55f, 1.75f);

				if (_manager == null) {
					player.closeInventory();
					player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_BREAK, 1f, 0.6f);
					boolean thai = LangManager.get().isThai(player);
					player.sendMessage(com.houzicore.shared.common.util.F.main("Shop", thai ? "§cเกิดข้อผิดพลาดภายใน UI: ไม่พบปลั๊กอินหลัก" : "§cUI internal error: plugin instance missing."));
					return;
				}
				player.closeInventory();
				_manager.performQuickMatch(player, _serverNpcKey);
			}
		});

		// Slot 24: Select Server
		addButton(24, new ShopItem(Material.COMPASS, (byte)0, ChatColor.RESET + C.cYellow + C.Bold + LangManager.get().get(getPlayer(), "server.selector.select_server.name"),
				new String[] {
						ChatColor.RESET + "",
						ChatColor.RESET + C.cGray + LangManager.get().get(getPlayer(), "server.selector.select_server.desc.1"),
						ChatColor.RESET + C.cGray + LangManager.get().get(getPlayer(), "server.selector.select_server.desc.2"),
						ChatColor.RESET + LangManager.get().get(getPlayer(), "server.selector.select_server.desc.3", _manager.getOnlineShardCount(_serverNpcKey)),
						ChatColor.RESET + LangManager.get().get(getPlayer(), "server.selector.select_server.desc.4", _manager.getServerBrowserStatusText(getPlayer(), _serverNpcKey)),
						ChatColor.RESET + ""
				}, 1, false, false), new IButton()
		{
			@Override
			public void onClick(Player player, ClickType clickType)
			{
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.2f);
				player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 1.55f);
				getShop().openPageForPlayer(player, new ServerNpcPage(getPlugin(), getShop(), getClientManager(), getDonationManager(), getName(), player, _serverNpcKey, new IButton()
				{
					@Override
					public void onClick(Player player, ClickType clickType)
					{
						getShop().openPageForPlayer(player, new ServerNpcSelectionPage(getPlugin(), getShop(), getClientManager(), getDonationManager(), getName(), player, _serverNpcKey));
					}
				}));
			}
		});

		addButton(22, new ShopItem(Material.DIAMOND, (byte)0, ChatColor.RESET + com.houzicore.shared.common.util.C.cAqua + com.houzicore.shared.common.util.C.Bold + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "server.selector.request.name"),
				new String[] {
						ChatColor.RESET + "",
						ChatColor.RESET + com.houzicore.shared.common.util.C.cGray + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "server.selector.request.desc.1"),
						ChatColor.RESET + com.houzicore.shared.common.util.C.cGray + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "server.selector.request.desc.2"),
						ChatColor.RESET + com.houzicore.shared.common.util.C.cGray + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "server.selector.request.desc.3"),
						ChatColor.RESET + "",
						ChatColor.RESET + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "server.selector.request.desc.4", _serverNpcKey)
				}, 1, false, false), new IButton()
		{
			@Override
			public void onClick(Player player, org.bukkit.event.inventory.ClickType clickType)
			{
				if (!getClientManager().Get(player).GetRank().Has(player, com.houzicore.shared.common.Rank.DIVINE, true)) {
					player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_BREAK, 1f, 0.6f);
					return;
				}
				
				player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.85f, 1.15f);
				player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.55f, 1.75f);
				player.closeInventory();
				_manager.requestServer(player, _serverNpcKey);
			}
		});

		// Go Back to close
		addButton(40, new ShopItem(Material.RED_BED, com.houzicore.shared.common.util.C.cGray + " \u21FD " + com.houzicore.shared.core.lang.LangManager.get().get(getPlayer(), "server.lobby.go_back"), new String[]{}, 1, false), new IButton()
		{
			@Override
			public void onClick(Player player, ClickType clickType)
			{
				player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.65f, 0.9f);
				player.closeInventory();
			}
		});
	}
}
