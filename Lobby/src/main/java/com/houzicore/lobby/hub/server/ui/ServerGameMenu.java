package com.houzicore.lobby.hub.server.ui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.itemstack.ItemStackFactory;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.shared.core.lang.LangManager;
import com.houzicore.lobby.hub.server.ServerManager;

import com.houzicore.lobby.hub.server.ui.button.SelectBHButton;
import com.houzicore.lobby.hub.server.ui.button.SelectMINButton;
import com.houzicore.lobby.hub.server.ui.button.SelectPLAYERButton;
import com.houzicore.lobby.hub.server.ui.button.SelectSGButton;

public class ServerGameMenu extends ShopPageBase<ServerManager, QuickShop>
{
	private List<ItemStack> _minigameCycle = new ArrayList<ItemStack>();
	private int _minigameIndex;
	
	public ServerGameMenu(ServerManager plugin, QuickShop quickShop, CoreClientManager clientManager, DonationManager donationManager, String name, Player player)
	{
		super(plugin, quickShop, clientManager, donationManager, com.houzicore.shared.common.util.UtilText.toSmallCaps(name), player, 54);
		
		createMinigameCycle();
		buildPage();
	}

	@Override
	protected void buildPage()
	{
		clear();
		createMinigameCycle();

		boolean sgOnline = getPlugin().isGroupOnline("HG", "Primal");
		boolean bhOnline = getPlugin().isGroupOnline("BH", "HideSeek", "HS");

		String onlineTag = LangManager.get().get(getPlayer(), "game_menu.status.online");
		if (onlineTag == null || onlineTag.isEmpty() || onlineTag.equals("game_menu.status.online")) onlineTag = "§a● §aOnline";
		String offlineTag = LangManager.get().get(getPlayer(), "game_menu.status.offline");
		if (offlineTag == null || offlineTag.isEmpty() || offlineTag.equals("game_menu.status.offline")) offlineTag = "§c● §cOffline";

		String sgStatus = sgOnline ? onlineTag : offlineTag;
		String bhStatus = bhOnline ? onlineTag : offlineTag;

		// Slot 20: Survival Primal Game
		setItem(20, hideInfo(ItemStackFactory.Instance.CreateStack(Material.IRON_SWORD, (byte)0, 1, 
				LangManager.get().get(getPlayer(), "game_menu.sg.name"), new String[] 
				{ 
					C.cDGray + C.Strike + "                                    ",
					"",
					LangManager.get().get(getPlayer(), "game_menu.sg.desc.1"),
					LangManager.get().get(getPlayer(), "game_menu.sg.desc.2"),
					LangManager.get().get(getPlayer(), "game_menu.sg.desc.3"),
					"",
					LangManager.get().get(getPlayer(), "game_menu.best_route", getPlugin().getQuickMatchRecommendationText(getPlayer(), "HG")),
					LangManager.get().get(getPlayer(), "game_menu.live_status", getPlugin().getQuickMatchAvailabilityText(getPlayer(), "HG")),
					sgStatus + "  §7" + LangManager.get().get(getPlayer(), "game_menu.join_players", getPlugin().getGroupTagPlayerCount("HG")),
					"",
					C.cDGray + C.Strike + "                                    "
				})));

		// Slot 22: Arcade (Mixed Games Cycle)
		setItem(22, _minigameCycle.get(_minigameIndex));

		// Slot 24: Prop Rush
		setItem(24, ItemStackFactory.Instance.CreateStack(Material.GRASS_BLOCK, (byte)0, 1, 
				LangManager.get().get(getPlayer(), "game_menu.block_hunt.name"), new String[] 
				{ 
					"",
					LangManager.get().get(getPlayer(), "game_menu.block_hunt.desc.1"),
					LangManager.get().get(getPlayer(), "game_menu.block_hunt.desc.2"),
					LangManager.get().get(getPlayer(), "game_menu.block_hunt.desc.3"),
					"",
					LangManager.get().get(getPlayer(), "game_menu.best_route", getPlugin().getQuickMatchRecommendationText(getPlayer(), "BH")),
					LangManager.get().get(getPlayer(), "game_menu.live_status", getPlugin().getQuickMatchAvailabilityText(getPlayer(), "BH")),
					bhStatus + "  §7" + LangManager.get().get(getPlayer(), "game_menu.join_players", getPlugin().getGroupTagPlayerCount("BH"))
				}));

		// Slot 31: Player Servers
		setItem(31, ItemStackFactory.Instance.CreateStack(Material.PLAYER_HEAD, (byte)3, 1, 
				LangManager.get().get(getPlayer(), "game_menu.player_servers.name"), new String[] 
				{ 
					"",
					LangManager.get().get(getPlayer(), "game_menu.player_servers.desc.1"),
					LangManager.get().get(getPlayer(), "game_menu.player_servers.desc.2"),
					LangManager.get().get(getPlayer(), "game_menu.player_servers.desc.3"),
					""
				}));

		getButtonMap().put(20, new SelectSGButton(this));
		getButtonMap().put(22, new SelectMINButton(this));
		getButtonMap().put(24, new SelectBHButton(this));
		getButtonMap().put(31, new SelectPLAYERButton(this));

		// Premium Selector GUI layout redesign: Light Blue borders with Blue fill
		com.houzicore.shared.common.util.GuiUtil.fillBorders(getInventory(), Material.LIGHT_BLUE_STAINED_GLASS_PANE);
		com.houzicore.shared.common.util.GuiUtil.fillAllEmpty(getInventory(), Material.BLUE_STAINED_GLASS_PANE, " ");
	}

	private void createMinigameCycle()
	{
		_minigameCycle.clear();

		int playerCount = getPlugin().getGroupTagPlayerCount("MIN") +
						  getPlugin().getGroupTagPlayerCount("DR") +
						  getPlugin().getGroupTagPlayerCount("DE") + 
						  getPlugin().getGroupTagPlayerCount("PB") + 
						  getPlugin().getGroupTagPlayerCount("TF") + 
						  getPlugin().getGroupTagPlayerCount("RUN") + 
						  getPlugin().getGroupTagPlayerCount("SN") + 
						  getPlugin().getGroupTagPlayerCount("DT") + 
						  getPlugin().getGroupTagPlayerCount("SQ") +
						  getPlugin().getGroupTagPlayerCount("SA") +
						  getPlugin().getGroupTagPlayerCount("SS") + 
						  getPlugin().getGroupTagPlayerCount("OITQ");
		
		String name = LangManager.get().get(getPlayer(), "game_menu.arcade.name");
		String count = LangManager.get().get(getPlayer(), "game_menu.join_players", playerCount);
		String intro = LangManager.get().get(getPlayer(), "game_menu.arcade.desc.intro");
		String spotlight = LangManager.get().get(getPlayer(), "game_menu.arcade.spotlight", isThai() ? "กำลังเด่นตอนนี้" : "Spotlight right now");
		String quickHint = LangManager.get().get(getPlayer(), "game_menu.arcade.quick_hint", isThai() ? "ควิกแมตช์จะส่งคุณไปเซิร์ฟเวอร์ที่เปิดอยู่และคึกคักที่สุด" : "Quick Match favors the busiest open server.");
		String[] cycleKeys = new String[] {
				"game_menu.arcade.cycle.micro_battles",
				"game_menu.arcade.cycle.turf_wars",
				"game_menu.arcade.cycle.runner",
				"game_menu.arcade.cycle.dragon_escape",
				"game_menu.arcade.cycle.death_tag",
				"game_menu.arcade.cycle.snake"
		};
		String[] cycleDefaults = new String[] {
				"",
				"",
				"",
				"",
				"",
				""
		};

		Material[] cycleMaterials = new Material[]{
			Material.CRACKED_STONE_BRICKS,
			Material.GOLDEN_BOOTS,
			Material.DRAGON_EGG,
			Material.BOW,
			Material.LEATHER_BOOTS,
			Material.MILK_BUCKET
		};
		
		for (int i = 0; i < cycleMaterials.length; i++) {
			Material mat = cycleMaterials[i];
			List<String> lore = new ArrayList<String>();
			lore.add("");
			lore.add(intro);
			lore.add("");
			lore.add(C.cAqua + C.Bold + spotlight + ":");
			for (int keyIndex = 0; keyIndex < cycleKeys.length; keyIndex++) {
				String line = LangManager.get().get(getPlayer(), cycleKeys[keyIndex], cycleDefaults[keyIndex]);
				if (keyIndex == i % cycleKeys.length) {
					lore.add(C.cYellow + C.Bold + "• " + line);
				} else {
					lore.add(C.cGray + "• " + line);
				}
			}
			lore.add("");
			lore.add(LangManager.get().get(getPlayer(), "game_menu.best_route", getPlugin().getQuickMatchRecommendationText(getPlayer(), "MIN")));
			lore.add(LangManager.get().get(getPlayer(), "game_menu.live_status", getPlugin().getQuickMatchAvailabilityText(getPlayer(), "MIN")));
			lore.add(LangManager.get().get(getPlayer(), "game_menu.server_browser", getPlugin().getServerBrowserStatusText(getPlayer(), "MIN")));
			lore.add("");
			lore.add(C.cGray + quickHint);
			lore.add(count);

			_minigameCycle.add(ItemStackFactory.Instance.CreateStack(mat, (byte)0, 1, name,
					lore.toArray(new String[0])));
		}

		// Apply enchant glow to make it stand out!
		for (ItemStack stack : _minigameCycle) {
			org.bukkit.inventory.meta.ItemMeta m = stack.getItemMeta();
			m.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
			m.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
			stack.setItemMeta(m);
		}
	}

	public void Update()
	{
		_minigameIndex++;
		
		if (_minigameIndex >= _minigameCycle.size())
			_minigameIndex = 0;
		
		buildPage();
	}

	public void OpenMIN(Player player) { getPlugin().getMixedArcadeShop().attemptShopOpen(player); }
	public void OpenBH(Player player) { getPlugin().getBlockHuntShop().attemptShopOpen(player); }
	public void OpenSG(Player player) { getPlugin().getSurvivalGamesShop().attemptShopOpen(player); }
	public void openPlayerGames(Player player) { getPlugin().getPlayerGamesShop().attemptShopOpen(player); }

	private boolean isThai()
	{
		return LangManager.get().isThai(getPlayer());
	}
}
