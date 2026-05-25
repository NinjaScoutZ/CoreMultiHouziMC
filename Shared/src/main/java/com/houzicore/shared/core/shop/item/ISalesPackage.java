package com.houzicore.shared.core.shop.item;

import java.util.List;

import org.bukkit.entity.Player;

import com.houzicore.shared.account.CoreClient;
import org.bukkit.inventory.Inventory;

public interface ISalesPackage {
	List<Integer> AddToCategory(Inventory inventory, int slot);

	boolean CanFitIn(CoreClient player);

	void DeliverTo(Player player);

	void DeliverTo(Player player, int slot);

	int GetGemCost();

	String GetName();

	int GetSalesPackageId();

	boolean IsFree();

	void PurchaseBy(CoreClient player);

	int ReturnFrom(CoreClient player);
}
