package com.houzicore.shared.core.shop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.CurrencyType;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.npc.event.NpcDamageByEntityEvent;
import com.houzicore.shared.core.npc.event.NpcInteractEntityEvent;
import com.houzicore.shared.core.shop.page.ShopPageBase;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public abstract class ShopBase<PluginType extends MiniPlugin> implements Listener {
	private final NautHashMap<String, Long> _errorThrottling;
	private final NautHashMap<String, Long> _purchaseBlock;

	private final List<CurrencyType> _availableCurrencyTypes;

	private final PluginType _plugin;
	private final CoreClientManager _clientManager;
	private final DonationManager _donationManager;
	private final String _name;
	private final NautHashMap<String, ShopPageBase<PluginType, ? extends ShopBase<PluginType>>> _playerPageMap;

	private final HashSet<String> _openedShop = new HashSet<>();

	public ShopBase(PluginType plugin, CoreClientManager clientManager, DonationManager donationManager, String name,
			CurrencyType... currencyTypes) {
		_plugin = plugin;
		_clientManager = clientManager;
		_donationManager = donationManager;
		_name = name;

		_playerPageMap = new NautHashMap<>();
		_errorThrottling = new NautHashMap<>();
		_purchaseBlock = new NautHashMap<>();

		_availableCurrencyTypes = new ArrayList<>();
		_availableCurrencyTypes.addAll(Arrays.asList(currencyTypes));

		_plugin.registerEvents(this);
	}

	public void addPlayerProcessError(Player player) {
		if (_errorThrottling.containsKey(player.getName())
				&& System.currentTimeMillis() - _errorThrottling.get(player.getName()) <= 5000) {
			_purchaseBlock.put(player.getName(), System.currentTimeMillis());
		}

		_errorThrottling.put(player.getName(), System.currentTimeMillis());
	}

	public boolean attemptShopOpen(Player player) {
		if (_name.contains("Treasure")) {
			org.bukkit.Bukkit.getLogger().info("[TreasureDebug] attemptShopOpen player=" + player.getName() + " alreadyOpened=" + _openedShop.contains(player.getName()));
		}
		if (!_openedShop.contains(player.getName())) {
			if (!canOpenShop(player))
				return false;

			_openedShop.add(player.getName());

			openShopForPlayer(player);
			if (!_playerPageMap.containsKey(player.getName())) {
				_playerPageMap.put(player.getName(), buildPagesFor(player));
			}

			if (_name.contains("Treasure")) {
				org.bukkit.Bukkit.getLogger().info("[TreasureDebug] Opening page for player, inMap=" + _playerPageMap.containsKey(player.getName()));
			}
			openPageForPlayer(player, getOpeningPageForPlayer(player));

			return true;
		}

		return false;
	}

	private boolean attemptShopOpen(Player player, LivingEntity entity) {
		if (!_openedShop.contains(player.getName()) && entity.isCustomNameVisible() && entity.getCustomName() != null
				&& ChatColor.stripColor(entity.getCustomName()).equalsIgnoreCase(ChatColor.stripColor(_name))) {
			if (!canOpenShop(player))
				return false;

			_openedShop.add(player.getName());

			openShopForPlayer(player);
			if (!_playerPageMap.containsKey(player.getName())) {
				_playerPageMap.put(player.getName(), buildPagesFor(player));
			}

			openPageForPlayer(player, getOpeningPageForPlayer(player));

			return true;
		}

		return false;
	}

	protected abstract ShopPageBase<PluginType, ? extends ShopBase<PluginType>> buildPagesFor(Player player);

	protected boolean canOpenShop(Player player) {
		return true;
	}

	public boolean canPlayerAttemptPurchase(Player player) {
		return !_purchaseBlock.containsKey(player.getName())
				|| System.currentTimeMillis() - _purchaseBlock.get(player.getName()) > 10000;
	}

	protected void closeShopForPlayer(Player player) {
	}

	public List<CurrencyType> getAvailableCurrencyTypes() {
		return _availableCurrencyTypes;
	}

	protected CoreClientManager getClientManager() {
		return _clientManager;
	}

	protected DonationManager getDonationManager() {
		return _donationManager;
	}

	protected String getName() {
		return _name;
	}

	protected HashSet<String> getOpenedShop() {
		return _openedShop;
	}

	protected ShopPageBase<PluginType, ? extends ShopBase<PluginType>> getOpeningPageForPlayer(Player player) {
		return _playerPageMap.get(player.getName());
	}

	public NautHashMap<String, ShopPageBase<PluginType, ? extends ShopBase<PluginType>>> getPageMap() {
		return _playerPageMap;
	}

	protected NautHashMap<String, ShopPageBase<PluginType, ? extends ShopBase<PluginType>>> getPlayerPageMap() {
		return _playerPageMap;
	}

	protected PluginType getPlugin() {
		return _plugin;
	}

	public boolean isPlayerInShop(Player player) {
		return _playerPageMap.containsKey(player.getName());
	}

	private boolean isCorrectPage(Player player, InventoryClickEvent event) {
		if (!_playerPageMap.containsKey(player.getName())) return false;
		return isCorrectPageByHolder(player, event.getInventory());
	}

	private boolean isCorrectPageByHolder(Player player, org.bukkit.inventory.Inventory inv) {
		if (!_playerPageMap.containsKey(player.getName())) return false;
		ShopPageBase<PluginType, ?> page = _playerPageMap.get(player.getName());
		if (page == null) return false;
		return inv.getHolder() == page;
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getWhoClicked() instanceof Player) {
			Player player = (Player) event.getWhoClicked();
			// Debug: log EVERY click for shops that have title "Treasure" 
			if (_name.contains("Treasure")) {
				boolean inMap = _playerPageMap.containsKey(player.getName());
				boolean inOpened = _openedShop.contains(player.getName());
				org.bukkit.Bukkit.getLogger().info("[TreasureDebug] onInventoryClick shop='" + _name 
					+ "' inMap=" + inMap + " inOpened=" + inOpened 
					+ " holder=" + (event.getInventory().getHolder() != null ? event.getInventory().getHolder().getClass().getSimpleName() : "null")
					+ " slot=" + event.getRawSlot() + " (" + player.getName() + ")");
			}
			if (isCorrectPage(player, event)) {
				event.setCancelled(true);
				
				// Item 78: Prevent Shift-Click, Hotbar Swap, and Dragging exploits
				if (event.getClick().isShiftClick() || event.getAction() == org.bukkit.event.inventory.InventoryAction.HOTBAR_SWAP || event.getAction() == org.bukkit.event.inventory.InventoryAction.HOTBAR_MOVE_AND_READD) {
					return;
				}

				// Item 77: Recharge Guard for GUI Multiclicks
				if (!com.houzicore.shared.recharge.Recharge.Instance.use(player, "GUI Click", 200, false, false)) {
					return;
				}

				_playerPageMap.get(player.getName()).playerClicked(event);
			}
		}
	}

	@EventHandler
	public void onInventoryDrag(org.bukkit.event.inventory.InventoryDragEvent event) {
		if (event.getWhoClicked() instanceof Player) {
			Player player = (Player) event.getWhoClicked();
			if (isCorrectPageByHolder(player, event.getInventory())) {
				event.setCancelled(true); // Item 78: Block dragging logic inside safe GUIs
			}
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (event.getPlayer() instanceof Player) {
			Player player = (Player) event.getPlayer();
			boolean holderMatch = isCorrectPageByHolder(player, event.getInventory());
			if (_name.contains("Treasure")) {
				org.bukkit.Bukkit.getLogger().info("[TreasureDebug] onInventoryClose holderMatch=" + holderMatch + " inMap=" + _playerPageMap.containsKey(player.getName()) + " (" + player.getName() + ")");
			}
			if (holderMatch) {
				_playerPageMap.get(player.getName()).playerClosed();
				_playerPageMap.get(player.getName()).dispose();

				_playerPageMap.remove(player.getName());

				closeShopForPlayer(player);

				_openedShop.remove(player.getName());
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onInventoryOpen(InventoryOpenEvent event) {
		if (!event.isCancelled())
			return;

		if (event.getPlayer() instanceof Player) {
			Player player = (Player) event.getPlayer();
			if (isCorrectPageByHolder(player, event.getInventory())) {
				_playerPageMap.get(player.getName()).playerClosed();
				_playerPageMap.get(player.getName()).dispose();

				_playerPageMap.remove(player.getName());

				closeShopForPlayer(player);

				_openedShop.remove(player.getName());
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerDamageEntity(NpcDamageByEntityEvent event) {
		if (event.getDamager() instanceof Player) {
			if (attemptShopOpen((Player) event.getDamager(), event.getNpc())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerInteractEntity(NpcInteractEntityEvent event) {
		if (attemptShopOpen(event.getPlayer(), event.getNpc())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (_playerPageMap.containsKey(event.getPlayer().getName())) {
			_playerPageMap.get(event.getPlayer().getName()).playerClosed();
			_playerPageMap.get(event.getPlayer().getName()).dispose();

			event.getPlayer().closeInventory();
			closeShopForPlayer(event.getPlayer());

			_playerPageMap.remove(event.getPlayer().getName());

			_openedShop.remove(event.getPlayer().getName());
		}
	}

	public void openPageForPlayer(Player player, ShopPageBase<PluginType, ? extends ShopBase<PluginType>> page) {
		if (_playerPageMap.containsKey(player.getName())) {
			_playerPageMap.get(player.getName()).playerClosed();
		}

		setCurrentPageForPlayer(player, page);
		player.closeInventory();

		// Open the new inventory on the next tick to avoid Paper's close/open conflict
		_plugin.getPlugin().getServer().getScheduler().runTask(_plugin.getPlugin(), () -> {
			if (player.isOnline()) {
				player.openInventory(page.getInventory());
				page.playerOpened();
			}
		});
	}

	protected void openShopForPlayer(Player player) {
	}

	public void setCurrentPageForPlayer(Player player, ShopPageBase<PluginType, ? extends ShopBase<PluginType>> page) {
		_playerPageMap.put(player.getName(), page);
	}

	private int _tickCounter = 0;

	@EventHandler
	public void onUpdate(UpdateEvent event) {
		if (event.getType() != UpdateType.TICK) return;
		_tickCounter++;

		for (ShopPageBase<PluginType, ? extends ShopBase<PluginType>> page : new ArrayList<>(_playerPageMap.values())) {
			if (page instanceof RefreshableGUI) {
				RefreshableGUI rGui = (RefreshableGUI) page;
				int refreshRate = Math.max(1, rGui.refreshRateTicks());
				if (_tickCounter % refreshRate == 0) {
					Player p = page.getPlayer();
					if (p != null && p.isOnline() && p.getOpenInventory() != null && p.getOpenInventory().getTopInventory() != null) {
						if (p.getOpenInventory().getTopInventory().getHolder() == page) {
							rGui.refreshItems(p);
						}
					}
				}
			}
		}
	}
}
