package com.houzicore.shared.core.inventory;

import com.houzicore.shared.core.inventory.data.Item;

public class ClientItem {
	public Item Item;
	public int Count;

	public ClientItem(Item item, int count) {
		Item = item;
		Count = count;
	}
}
