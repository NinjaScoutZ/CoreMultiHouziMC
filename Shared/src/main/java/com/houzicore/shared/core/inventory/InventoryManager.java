package com.houzicore.shared.core.inventory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.MiniDbClientPlugin;
import com.houzicore.shared.account.CoreClientManager;
import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.core.inventory.command.GiveItemCommand;
import com.houzicore.shared.core.inventory.data.Category;
import com.houzicore.shared.core.inventory.data.InventoryRepository;
import com.houzicore.shared.core.inventory.data.Item;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class InventoryManager extends MiniDbClientPlugin<ClientInventory> {
	public static InventoryManager Instance;
	private static Object _inventoryLock = new Object();

	private final InventoryRepository _repository;

	private final NautHashMap<String, Item> _items = new NautHashMap<>();
	private final NautHashMap<String, Category> _categories = new NautHashMap<>();

	private final NautHashMap<Player, NautHashMap<String, NautHashMap<String, Integer>>> _inventoryQueue = new NautHashMap<>();

	public InventoryManager(JavaPlugin plugin, CoreClientManager clientManager) {
		super("Inventory Manager", plugin, clientManager);
		Instance = this;

		_repository = new InventoryRepository(plugin);

		Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(getPlugin(), new Runnable() {
			@Override
			public void run() {
				updateItems();
				updateCategories();
			}
		}, 20L);
	}

	@Override
	public void addCommands() {
		addCommand(new GiveItemCommand(this));
	}

	public void addItemToInventory(final Callback<Boolean> callback, final Player player, String category,
			final String item, final int count) {
		addItemToInventoryForOffline(new Callback<Boolean>() {
			@Override
			public void run(Boolean success) {
				if (!success) {

					if (_items.containsKey(item)) {
						Get(player).addItem(new ClientItem(_items.get(item), -count));
					}
				}

				if (callback != null) {
					callback.run(success);
				}
			}
		}, player.getUniqueId(), category, item, count);

	}

	public void addItemToInventory(final Player player, String category, final String item, final int count) {
		if (_items.containsKey(item)) {
			Get(player).addItem(new ClientItem(_items.get(item), count));
		}

		if (!_inventoryQueue.containsKey(player)) {
			_inventoryQueue.put(player, new NautHashMap<String, NautHashMap<String, Integer>>());
		}

		if (!_inventoryQueue.get(player).containsKey(category)) {
			_inventoryQueue.get(player).put(category, new NautHashMap<String, Integer>());
		}

		int totalAmount = count;

		if (_inventoryQueue.get(player).get(category).containsKey(item)) {
			totalAmount += _inventoryQueue.get(player).get(category).get(item);
		}

		_inventoryQueue.get(player).get(category).put(item, totalAmount);
	}

	public void addItemToInventoryForOffline(final Callback<Boolean> callback, final UUID uuid, final String category,
			final String item, final int count) {
		Bukkit.getServer().getScheduler().runTaskAsynchronously(getPlugin(), new Runnable() {
			@Override
			public void run() {
				synchronized (_inventoryLock) {
					if (!_categories.containsKey(category)) {
						_repository.addCategory(category);
					}
				}

				updateCategories();

				synchronized (_inventoryLock) {
					if (!_items.containsKey(item)) {
						_repository.addItem(item, _categories.get(category).Id);
					}
				}

				updateItems();

				synchronized (_inventoryLock) {
					final boolean success = _repository.incrementClientInventoryItem(
							ClientManager.getCachedClientAccountId(uuid), _items.get(item).Id, count);

					if (callback != null) {
						Bukkit.getServer().getScheduler().runTask(getPlugin(), new Runnable() {
							@Override
							public void run() {
								callback.run(success);
							}
						});
					}
				}
			}
		});
	}

	@Override
	protected ClientInventory AddPlayer(String player) {
		return new ClientInventory();
	}

	public Item getItem(String itemName) {
		Item item = null;

		for (final Map.Entry<String, Item> entry : _items.entrySet()) {
			final String name = entry.getKey();

			if (name.equalsIgnoreCase(itemName)) {
				item = entry.getValue();
			}
		}

		return item;
	}

	@Override
	public String getQuery(int accountId, String uuid, String name) {
		return "SELECT items.name, ic.name as category, count FROM accountInventory AS ai INNER JOIN items ON items.id = ai.itemId INNER JOIN itemCategories AS ic ON ic.id = items.categoryId WHERE ai.accountId = '"
				+ accountId + "';";
	}

	@Override
	public void processLoginResultSet(String playerName, int accountId, ResultSet resultSet) throws SQLException {
		Set(playerName, _repository.loadClientInformation(resultSet));
	}

	private void updateCategories() {
		final List<Category> categories = _repository.retrieveCategories();

		synchronized (_inventoryLock) {
			for (final Category category : categories) {
				_categories.put(category.Name, category);
			}
		}
	}

	@EventHandler
	public void updateInventoryQueue(UpdateEvent event) {
		if (event.getType() != UpdateType.SEC)
			return;

		for (final Player player : _inventoryQueue.keySet()) {
			for (final String category : _inventoryQueue.get(player).keySet()) {
				for (final String item : _inventoryQueue.get(player).get(category).keySet()) {
					final int count = _inventoryQueue.get(player).get(category).get(item);

					addItemToInventoryForOffline(new Callback<Boolean>() {
						@Override
						public void run(Boolean success) {
							if (!success) {

								if (_items.containsKey(item)) {
									Get(player).addItem(new ClientItem(_items.get(item), -count));
								}
							}
						}
					}, player.getUniqueId(), category, item, count);
				}
			}

			// Clean
			_inventoryQueue.get(player).clear();
		}

		// Clean
		_inventoryQueue.clear();
	}

	private void updateItems() {
		final List<Item> items = _repository.retrieveItems();

		synchronized (_inventoryLock) {
			for (final Item item : items) {
				_items.put(item.Name, item);
			}
		}
	}

	public boolean validCategory(String category) {
		synchronized (_inventoryLock) {
			return _categories.containsKey(category);
		}
	}

	public boolean validItem(String item) {
		synchronized (_inventoryLock) {
			return _items.containsKey(item);
		}
	}
}
