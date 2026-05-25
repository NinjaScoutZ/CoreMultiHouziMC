package com.houzicore.shared.core.cosmetic.ui.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.core.pet.PetManager;
import com.houzicore.shared.core.shop.item.IButton;
import com.houzicore.shared.core.shop.page.ShopPageBase;

public class DeactivatePetButton implements IButton {
	private final ShopPageBase<?, ?> _page;
	private final PetManager _petManager;

	public DeactivatePetButton(ShopPageBase<?, ?> page, PetManager petManager) {
		_page = page;
		_petManager = petManager;
	}

	@Override
	public void onClick(Player player, ClickType clickType) {
		_page.playAcceptSound(player);
		_petManager.RemovePet(player, true);
		_page.refresh();
	}
}
