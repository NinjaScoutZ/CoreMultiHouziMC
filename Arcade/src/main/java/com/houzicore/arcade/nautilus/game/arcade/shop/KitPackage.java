package com.houzicore.arcade.nautilus.game.arcade.shop;

import org.bukkit.entity.Player;

import com.houzicore.arcade.nautilus.game.arcade.kit.Kit;
import com.houzicore.shared.common.CurrencyType;
import com.houzicore.shared.core.shop.item.SalesPackageBase;

public class KitPackage extends SalesPackageBase
{
	public KitPackage(String gameName, Kit kit, Player player)
	{
		super(gameName + " " + kit.GetName(), kit.getDisplayMaterial(), kit.GetDesc(player));
		KnownPackage = false;
		CurrencyCostMap.put(CurrencyType.Essence, kit.GetCost());
	}

	@Override
	public void Sold(Player player, CurrencyType currencyType)
	{

	}
}
