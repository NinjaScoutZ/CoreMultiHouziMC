package com.houzicore.shared.core.inventory.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.plugin.java.JavaPlugin;

import com.houzicore.shared.core.database.DBPool;
import com.houzicore.shared.core.database.RepositoryBase;
import com.houzicore.shared.core.database.ResultSetCallable;
import com.houzicore.shared.core.database.column.ColumnInt;
import com.houzicore.shared.core.database.column.ColumnVarChar;
import com.houzicore.shared.core.inventory.ClientInventory;
import com.houzicore.shared.core.inventory.ClientItem;

public class InventoryRepository extends RepositoryBase {
	private static String INSERT_ITEM = "INSERT INTO items (name, categoryId) VALUES (?, ?);";
	private static String RETRIEVE_ITEMS = "SELECT items.id, items.name, itemCategories.name FROM items INNER JOIN itemCategories ON itemCategories.id = items.categoryId;";

	private static String INSERT_CATEGORY = "INSERT INTO itemCategories (name) VALUES (?);";
	private static String RETRIEVE_CATEGORIES = "SELECT id, name FROM itemCategories;";

	private static String INSERT_CLIENT_INVENTORY = "INSERT INTO accountInventory (accountId, itemId, count) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE count=count + VALUES(count);";
	private static String UPDATE_CLIENT_INVENTORY = "UPDATE accountInventory SET count = count + ? WHERE accountId = ? AND itemId = ?;";

	public InventoryRepository(JavaPlugin plugin) {
		super(plugin, DBPool.ACCOUNT);
	}

	public void addCategory(String name) {
		executeUpdate(INSERT_CATEGORY, new ColumnVarChar("name", 100, name));
	}

	public void addItem(String name, int categoryId) {
		executeUpdate(INSERT_ITEM, new ColumnVarChar("name", 100, name), new ColumnInt("categoryId", categoryId));
	}

	public boolean incrementClientInventoryItem(int accountId, int itemId, int count) {
		// 
		if (executeUpdate(UPDATE_CLIENT_INVENTORY, new ColumnInt("count", count), new ColumnInt("id", accountId),
				new ColumnInt("itemid", itemId)) < 1)
			// 
			return executeUpdate(INSERT_CLIENT_INVENTORY, new ColumnInt("id", accountId),
					new ColumnInt("itemid", itemId), new ColumnInt("count", count)) > 0;
		else
			return true;
	}

	@Override
	protected void initialize() {
		/*
		 * executeUpdate(CREATE_INVENTORY_CATEGORY_TABLE);
		 * executeUpdate(CREATE_INVENTORY_TABLE);
		 * executeUpdate(CREATE_INVENTORY_RELATION_TABLE);
		 */
	}

	public ClientInventory loadClientInformation(ResultSet resultSet) throws SQLException {
		final ClientInventory clientInventory = new ClientInventory();

		while (resultSet.next()) {
			clientInventory.addItem(
					new ClientItem(new Item(resultSet.getString(1), resultSet.getString(2)), resultSet.getInt(3)));
		}

		return clientInventory;
	}

	public List<Category> retrieveCategories() {
		final List<Category> categories = new ArrayList<>();

		executeQuery(RETRIEVE_CATEGORIES, new ResultSetCallable() {
			@Override
			public void processResultSet(ResultSet resultSet) throws SQLException {
				while (resultSet.next()) {
					categories.add(new Category(resultSet.getInt(1), resultSet.getString(2)));
				}
			}
		});

		return categories;
	}

	public List<Item> retrieveItems() {
		final List<Item> items = new ArrayList<>();

		executeQuery(RETRIEVE_ITEMS, new ResultSetCallable() {
			@Override
			public void processResultSet(ResultSet resultSet) throws SQLException {
				while (resultSet.next()) {
					items.add(new Item(resultSet.getInt(1), resultSet.getString(2), resultSet.getString(3)));
				}
			}
		});

		return items;
	}

	@Override
	protected void update() {
	}
}
