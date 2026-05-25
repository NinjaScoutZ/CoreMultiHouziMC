package com.houzicore.shared.core.pet;

import org.bukkit.entity.EntityType;

import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.core.pet.repository.token.ClientPetToken;
import com.houzicore.shared.core.pet.repository.token.PetToken;

public class PetClient {
	private NautHashMap<EntityType, String> _pets;
	private int _petNameTagCount;

	public Integer GetPetNameTagCount() {
		return _petNameTagCount;
	}

	public NautHashMap<EntityType, String> GetPets() {
		return _pets;
	}

	public void Load(ClientPetToken token) {
		_pets = new NautHashMap<>();

		for (final PetToken petToken : token.Pets) {
			if (petToken.PetName == null) {
				petToken.PetName = Enum.valueOf(EntityType.class, petToken.PetType).getName();
			}

			_pets.put(Enum.valueOf(EntityType.class, petToken.PetType), petToken.PetName);
		}

		_petNameTagCount = Math.max(0, token.PetNameTagCount);
	}

	public void SetPetNameTagCount(int count) {
		_petNameTagCount = count;
	}
}
