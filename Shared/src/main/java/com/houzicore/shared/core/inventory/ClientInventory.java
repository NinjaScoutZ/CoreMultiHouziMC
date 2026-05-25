package com.houzicore.shared.core.inventory;

import com.houzicore.shared.common.util.NautHashMap;

public class ClientInventory {
	public NautHashMap<String, ClientItem> Items = new NautHashMap<>();

	public void addItem(ClientItem item) {
		if (!Items.containsKey(item.Item.Name)) {
			Items.put(item.Item.Name, new ClientItem(item.Item, 0));
		}

		Items.get(item.Item.Name).Count += item.Count;
	}

	public int getItemCount(String name) {
		return Items.containsKey(name) ? Items.get(name).Count : 0;
	}

	public void removeItem(ClientItem item) {
		if (!Items.containsKey(item.Item.Name))
			return;

		Items.get(item.Item.Name).Count -= item.Count;

		if (Items.get(item.Item.Name).Count == 0) {
			Items.remove(item.Item.Name);
		}
	}
}
