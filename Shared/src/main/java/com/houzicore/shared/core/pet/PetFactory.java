package com.houzicore.shared.core.pet;

import java.util.Collection;
import com.houzicore.shared.common.util.NautHashMap;
import com.houzicore.shared.core.cosmetic.CosmeticProgression;
import com.houzicore.shared.core.pet.repository.PetRepository;
import com.houzicore.shared.core.pet.types.Elf;
import com.houzicore.shared.core.pet.types.Pumpkin;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class PetFactory {
	private final NautHashMap<EntityType, Pet> _pets;
	private final NautHashMap<Material, PetExtra> _petExtras;

	public PetFactory(PetRepository repository) {
		_pets = new NautHashMap<>();
		_petExtras = new NautHashMap<>();

		CreatePets();
		CreatePetExtras();
	}

	private void CreatePetExtras() {
		_petExtras.put(Material.OAK_SIGN, new PetExtra("Name Tag", Material.NAME_TAG, 100));
	}

	private void CreatePets() {
		_pets.put(EntityType.ZOMBIE, new Pumpkin());
		_pets.put(EntityType.VILLAGER, new Elf());
		_pets.put(EntityType.PIG, createPet("Pig", EntityType.PIG));
		_pets.put(EntityType.SHEEP, createPet("Sheep", EntityType.SHEEP));
		_pets.put(EntityType.COW, createPet("Cow", EntityType.COW));
		_pets.put(EntityType.CHICKEN, createPet("Chicken", EntityType.CHICKEN));
		_pets.put(EntityType.WOLF, createPet("Dog", EntityType.WOLF));
		_pets.put(EntityType.OCELOT, createPet("Cat", EntityType.OCELOT));
		_pets.put(EntityType.MOOSHROOM, createPet("Mooshroom", EntityType.MOOSHROOM));
		_pets.put(EntityType.RABBIT, createPet("Rabbit", EntityType.RABBIT));
		_pets.put(EntityType.FOX, createPet("Fox", EntityType.FOX));
		_pets.put(EntityType.BEE, createPet("Bee", EntityType.BEE));
		_pets.put(EntityType.PARROT, createPet("Parrot", EntityType.PARROT));
		_pets.put(EntityType.PANDA, createPet("Panda", EntityType.PANDA));
		_pets.put(EntityType.TURTLE, createPet("Turtle", EntityType.TURTLE));
		_pets.put(EntityType.AXOLOTL, createPet("Axolotl", EntityType.AXOLOTL));
		_pets.put(EntityType.FROG, createPet("Frog", EntityType.FROG));
		_pets.put(EntityType.CAMEL, createPet("Camel", EntityType.CAMEL));
		_pets.put(EntityType.SNIFFER, createPet("Sniffer", EntityType.SNIFFER));
		_pets.put(EntityType.WITHER, createPet("Widder", EntityType.WITHER));
	}

	private Pet createPet(String name, EntityType type) {
		return new Pet(name, type, CosmeticProgression.getPrice(CosmeticProgression.getPetRarity(name)));
	}

	public Collection<PetExtra> GetPetExtraBySalesId(int salesId) {
		return _petExtras.values();
	}

	public Collection<PetExtra> GetPetExtras() {
		return _petExtras.values();
	}

	public Collection<Pet> GetPets() {
		return _pets.values();
	}
}
