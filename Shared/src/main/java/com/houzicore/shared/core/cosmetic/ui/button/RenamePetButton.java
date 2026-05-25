package com.houzicore.shared.core.cosmetic.ui.button;

import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import com.houzicore.shared.core.cosmetic.ui.page.PetPage;
import com.houzicore.shared.core.pet.Pet;
import com.houzicore.shared.core.shop.item.IButton;

public class RenamePetButton implements IButton {
	private final PetPage _page;

	public RenamePetButton(PetPage page) {
		_page = page;
	}

	@Override
	public void onClick(Player player, ClickType clickType) {
		_page.playAcceptSound(player);
		final Creature currentPet = _page.getPlugin().getPetManager().getActivePet(player.getName());
		//_page.renamePet(player, new Pet(currentPet.getCustomName(), currentPet.getType(), 1), false);
	}
}
