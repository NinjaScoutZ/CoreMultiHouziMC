package com.houzicore.shared.core.pet;

import com.houzicore.shared.common.CurrencyType;
import com.houzicore.shared.core.pet.repository.token.PetExtraToken;
import com.houzicore.shared.core.shop.item.SalesPackageBase;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public class PetExtra extends SalesPackageBase {
	private final String _name;
	private final Material _material;

	public PetExtra(String name, Material material, int cost) {
		super(name, material, (byte) 0, new String[] {});

		_name = name;
		_material = material;
		CurrencyCostMap.put(CurrencyType.Coins, cost);

		KnownPackage = false;
		OneTimePurchaseOnly = false;
	}

	public Material GetMaterial() {
		return _material;
	}

	@Override
	public String GetName() {
		return _name;
	}

	@Override
	public void Sold(Player player, CurrencyType currencyType) {
	}

	public void Update(PetExtraToken token) {

	}
}
