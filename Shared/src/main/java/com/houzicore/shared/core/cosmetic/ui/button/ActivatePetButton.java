package com.houzicore.shared.core.cosmetic.ui.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.core.cosmetic.ui.page.Menu;
import com.houzicore.shared.core.cosmetic.ui.page.PetPage;
import com.houzicore.shared.core.pet.Pet;
import com.houzicore.shared.core.shop.item.IButton;

public class ActivatePetButton implements IButton {
	private final Pet _pet;
	private final PetPage _page;

	public ActivatePetButton(Pet pet, PetPage page) {
		_pet = pet;
		_page = page;
	}

	@Override
	public void onClick(Player player, ClickType clickType) {
		_page.playAcceptSound(player);
		_page.getPlugin().getPetManager().AddPetOwner(player, _pet.GetPetType(), player.getLocation());
		_page.getShop().openPageForPlayer(player, new Menu(_page.getPlugin(), _page.getShop(), _page.getClientManager(),
				_page.getDonationManager(), player));
	}
}
