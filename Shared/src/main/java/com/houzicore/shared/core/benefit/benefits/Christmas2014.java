package com.houzicore.shared.core.benefit.benefits;

import com.houzicore.shared.core.benefit.BenefitManager;
import com.houzicore.shared.core.benefit.BenefitManagerRepository;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.Callback;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.inventory.InventoryManager;

import org.bukkit.entity.Player;

public class Christmas2014 extends BenefitBase {
	private final InventoryManager _inventoryManager;

	public Christmas2014(BenefitManager plugin, BenefitManagerRepository repository,
			InventoryManager inventoryManager) {
		super(plugin, "Christmas2014", repository);

		_inventoryManager = inventoryManager;
	}

	@Override
	public void rewardPlayer(final Player player) {
		_inventoryManager.addItemToInventory(new Callback<Boolean>() {
			@Override
			public void run(Boolean success) {
				if (success) {
					UtilPlayer.message(player, C.cPurple + C.Strike + "=============================================");
					UtilPlayer.message(player, "");
					UtilPlayer.message(player, C.cRed + "MERRY CHRISTMAS");
					UtilPlayer.message(player, "You received 2 Treasure Keys!");
					UtilPlayer.message(player, "");
					UtilPlayer.message(player, C.cPurple + C.Strike + "=============================================");
				}
			}
		}, player, "Treasure", "Treasure Key", 2);
	}
}
