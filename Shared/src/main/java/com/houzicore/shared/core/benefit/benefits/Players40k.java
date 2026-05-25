package com.houzicore.shared.core.benefit.benefits;

import com.houzicore.shared.core.benefit.BenefitManager;
import com.houzicore.shared.core.benefit.BenefitManagerRepository;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.inventory.InventoryManager;

import org.bukkit.entity.Player;

public class Players40k extends BenefitBase {
	private final InventoryManager _inventoryManager;

	public Players40k(BenefitManager plugin, BenefitManagerRepository repository, InventoryManager inventoryManager) {
		super(plugin, "Players40k", repository);

		_inventoryManager = inventoryManager;
	}

	@Override
	public void rewardPlayer(final Player player) {
		_inventoryManager.addItemToInventory(new Callback<Boolean>() {
			@Override
			public void run(Boolean success) {
				if (success) {
					UtilPlayer.message(player, C.cGold + C.Strike + "=============================================");
					UtilPlayer.message(player, "");
					UtilPlayer.message(player, "To celebrate hitting 40,000 players online,");
					UtilPlayer.message(player, "everyone receives a prize! You're awesome!");
					UtilPlayer.message(player, "");
					UtilPlayer.message(player, "You received 1 Ancient Chest!");
					UtilPlayer.message(player, "");
					UtilPlayer.message(player, C.cGold + C.Strike + "=============================================");
				}
			}
		}, player, "Treasure", "Ancient Chest", 1);
	}
}
