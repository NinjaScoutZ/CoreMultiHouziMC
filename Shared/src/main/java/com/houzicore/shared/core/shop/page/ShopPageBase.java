package com.houzicore.shared.core.shop.page;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.account.CoreClient;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.CurrencyType;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.common.util.UtilInv;
import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.shop.ShopBase;
import com.houzicore.shared.core.shop.item.IButton;

public abstract class ShopPageBase<PluginType extends MiniPlugin, ShopType extends ShopBase<PluginType>>
		implements Listener, InventoryHolder {
	private PluginType _plugin;
	private CoreClientManager _clientManager;
	private DonationManager _donationManager;
	private ShopType _shop;
	private Player _player;
	private CoreClient _client;
	private CurrencyType _currencyType;
	private NautHashMap<Integer, IButton> _buttonMap;
	private final boolean _showCurrency = false;

	private Inventory _inventory;
	private final String _name;

	private final int _currencySlot = 4;

	public ShopPageBase(PluginType plugin, ShopType shop, CoreClientManager clientManager,
			DonationManager donationManager, String name, Player player) {
		this(plugin, shop, clientManager, donationManager, name, player, 54);
	}

	public ShopPageBase(PluginType plugin, ShopType shop, CoreClientManager clientManager,
			DonationManager donationManager, String name, Player player, int slots) {
		
		_plugin = plugin;
		_clientManager = clientManager;
		_donationManager = donationManager;
		_shop = shop;
		_player = player;
		_name = name;
		_buttonMap = new NautHashMap<>();

		net.kyori.adventure.text.Component title = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(name);
		_inventory = Bukkit.createInventory(this, slots, title);

		_client = _clientManager.Get(player);

		if (shop.getAvailableCurrencyTypes().size() > 0) {
			_currencyType = shop.getAvailableCurrencyTypes().get(0);
		}
	}

	@Override
	public Inventory getInventory() {
		return _inventory;
	}

	public String getName() {
		return _name;
	}

	public String getTitle() {
		return _name;
	}

	protected void addButton(int slot, ItemStack item, IButton button) {
		addItem(slot, item);
		_buttonMap.put(slot, button);
	}

	protected void addButtonFakeCount(int slot, ItemStack item, IButton button, int fakeItemCount) {
		addItemFakeCount(slot, item, fakeItemCount);
		_buttonMap.put(slot, button);
	}

	protected void addGlow(int slot) {
		if (getItem(slot) != null) {
			UtilInv.addDullEnchantment(getItem(slot));
		}
	}

	public ItemStack getItem(int slot) {
		return getInventory().getItem(slot);
	}

	protected void addItem(int slot, ItemStack item) {
		if (slot > getInventory().getSize() - 1) {
			_player.getInventory().setItem(getPlayerSlot(slot), item);
		} else {
			setItem(slot, item);
		}
	}

	public void setItem(int slot, ItemStack item) {
		getInventory().setItem(slot, item);
	}

	protected void addItemFakeCount(int slot, ItemStack item, int fakeCount) {
		item.setAmount(fakeCount);
		if (slot > getInventory().getSize() - 1) {
			_player.getInventory().setItem(getPlayerSlot(slot), item);
		} else {
			getInventory().setItem(slot, item);
		}
	}

	protected abstract void buildPage();

	protected void changeCurrency(Player player) {
		playAcceptSound(player);

		final int currentIndex = _shop.getAvailableCurrencyTypes().indexOf(_currencyType);

		if (currentIndex + 1 < _shop.getAvailableCurrencyTypes().size()) {
			_currencyType = _shop.getAvailableCurrencyTypes().get(currentIndex + 1);
		} else {
			_currencyType = _shop.getAvailableCurrencyTypes().get(0);
		}
	}

	public void clearPage() {
		getInventory().clear();
		_buttonMap.clear();
	}

	public void dispose() {
		_player = null;
		_client = null;
		_shop = null;
		_plugin = null;
	}

	protected NautHashMap<Integer, IButton> getButtonMap() {
		return _buttonMap;
	}

	protected CoreClient getClient() {
		return _client;
	}

	public CoreClientManager getClientManager() {
		return _clientManager;
	}

	protected int getCurrencySlot() {
		return _currencySlot;
	}

	protected CurrencyType getCurrencyType() {
		return _currencyType;
	}

	public DonationManager getDonationManager() {
		return _donationManager;
	}

	public Player getPlayer() {
		return _player;
	}

	protected int getPlayerSlot(int slot) {
		return slot >= getInventory().getSize() + 27 ? slot - (getInventory().getSize() + 27) : slot - (getInventory().getSize() - 9);
	}

	public PluginType getPlugin() {
		return _plugin;
	}

	public ShopType getShop() {
		return _shop;
	}

	/**
	 * Hide all item info flags (attributes, enchants, etc) — HouziCore UI standard.
	 */
	protected ItemStack hideInfo(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			meta.addItemFlags(ItemFlag.values());
			item.setItemMeta(meta);
		}
		return item;
	}

	public void playAcceptSound(Player player) {
		player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1.6f);
	}

	public void playDenySound(Player player) {
		player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 0.6f);
	}

	public void playerClicked(InventoryClickEvent event) {
		if (_buttonMap.containsKey(event.getRawSlot())) {
			org.bukkit.Bukkit.getLogger().info("[ShopDebug] Calling onClick for slot " + event.getRawSlot());
			_buttonMap.get(event.getRawSlot()).onClick(_player, event.getClick());
		} else if (event.getRawSlot() != -999) {
			org.bukkit.Bukkit.getLogger().info("[ShopDebug] Unmapped slot clicked: " + event.getRawSlot());
			if (event.getView().getTitle().equals(_name)
					&& (getInventory().getSize() <= event.getSlot() || getInventory().getItem(event.getSlot()) != null)) {
				playDenySound(_player);
			} else if (event.getClickedInventory() == _player.getInventory()
					&& _player.getInventory().getItem(event.getSlot()) != null) {
				playDenySound(_player);
			}
		}
	}

	public void playerClosed() {
	}

	public void playerOpened() {
	}

	public void playRemoveSound(Player player) {
		player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 0.6f);
	}

	public void refresh() {
		clearPage();
		buildPage();
	}

	public void clear() {
		clearPage();
	}

	public void clear(int slot) {
		getInventory().setItem(slot, null);
		_buttonMap.remove(slot);
	}

	public int getSize() {
		return getInventory().getSize();
	}

	protected void removeButton(int slot) {
		getInventory().setItem(slot, null);
		_buttonMap.remove(slot);
	}

	protected void setCurrencyType(CurrencyType type) {
		_currencyType = type;
	}

	public void setItem(int column, int row, ItemStack itemStack) {
		setItem(column + row * 9, itemStack);
	}

	protected boolean shouldShowCurrency() {
		return _showCurrency;
	}
}
