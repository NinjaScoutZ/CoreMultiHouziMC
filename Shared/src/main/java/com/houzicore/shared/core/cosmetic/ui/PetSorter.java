package com.houzicore.shared.core.cosmetic.ui;

import java.util.Comparator;

import com.houzicore.shared.core.pet.Pet;

public class PetSorter implements Comparator<Pet> {
	@Override
	public int compare(Pet a, Pet b) {
		if (a.GetPetType().getTypeId() < b.GetPetType().getTypeId())
			return -1;

		return 1;
	}
}
