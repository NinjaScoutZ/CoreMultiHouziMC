package com.houzicore.shared.core.pet;

import com.houzicore.shared.common.CurrencyType;
import com.houzicore.shared.core.pet.repository.token.PetSalesToken;
import com.houzicore.shared.core.shop.item.SalesPackageBase;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class Pet extends SalesPackageBase {
	private String _name;
	private final EntityType _petType;

	public Pet(String name, EntityType petType, int cost) {
		super(name, Material.SHEEP_SPAWN_EGG, (byte) petType.ordinal(), new String[] {});

		_name = name;
		_petType = petType;
		CurrencyCostMap.put(CurrencyType.Coins, cost);

		KnownPackage = false;
	}

	public String GetPetName() {
		return _name;
	}

	public EntityType GetPetType() {
		return _petType;
	}

	@Override
	public void Sold(Player player, CurrencyType currencyType) {

	}

	public void Update(PetSalesToken petToken) {
		_name = petToken.Name;
	}
}
