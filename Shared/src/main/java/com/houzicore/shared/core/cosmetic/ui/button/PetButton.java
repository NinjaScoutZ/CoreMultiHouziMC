package com.houzicore.shared.core.cosmetic.ui.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.core.pet.Pet;
import com.houzicore.shared.core.cosmetic.ui.page.PetPage;
import com.houzicore.shared.core.shop.item.IButton;

public class PetButton implements IButton {
	private final Pet _pet;
	private final PetPage _page;

	public PetButton(Pet pet, PetPage page) {
		_pet = pet;
		_page = page;
	}

	@Override
	public void onClick(Player player, ClickType clickType) {
		//_page.purchasePet(player, _pet);
	}
}
