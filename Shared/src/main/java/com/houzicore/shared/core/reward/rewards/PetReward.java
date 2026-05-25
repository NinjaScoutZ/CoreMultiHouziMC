package com.houzicore.shared.core.reward.rewards;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.houzicore.shared.core.donation.DonationManager;
import com.houzicore.shared.core.inventory.InventoryManager;
import com.houzicore.shared.core.pet.PetManager;
import com.houzicore.shared.core.pet.repository.token.PetChangeToken;
import com.houzicore.shared.core.pet.repository.token.PetToken;
import com.houzicore.shared.core.reward.RewardData;
import com.houzicore.shared.core.reward.RewardRarity;

/**
 * Created by shaun on 14-09-18.
 */
public class PetReward extends CosmeticReward {
	private final InventoryManager _inventoryManager;
	private final PetManager _petManager;
	private final EntityType _petEntity;

	public PetReward(PetManager petManager, InventoryManager inventoryManager, DonationManager donationManager,
			String name, String packageName, EntityType petEntity, RewardRarity rarity, int weight) {
		super(donationManager, name, packageName, new ItemStack(com.houzicore.shared.common.util.IdUtil.getSpawnEggMaterial(petEntity)), rarity,
				weight);

		_petManager = petManager;
		_inventoryManager = inventoryManager;
		_petEntity = petEntity;
	}

	@Override
	protected RewardData giveRewardCustom(Player player) {
		final PetChangeToken token = new PetChangeToken();
		token.Name = player.getName();
		token.PetType = _petEntity.toString();
		token.PetName = getUnlockId();

		final PetToken petToken = new PetToken();
		petToken.PetType = token.PetType;

		_petManager.GetRepository().AddPet(token);
		// _petManager.addPetOwnerToQueue(player.getName(), _petEntity);

		_petManager.Get(player).GetPets().put(_petEntity, token.PetName);

		_inventoryManager.addItemToInventory(player, "Pet", _petEntity.toString(), 1);

		return super.giveRewardCustom(player);
	}
}
