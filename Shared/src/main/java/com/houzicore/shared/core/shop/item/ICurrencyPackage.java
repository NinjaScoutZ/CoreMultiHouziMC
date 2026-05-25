package com.houzicore.shared.core.shop.item;

import com.houzicore.shared.common.CurrencyType;
import com.houzicore.shared.core.donation.repository.GameSalesPackageToken;

public interface ICurrencyPackage {
	int GetCost(CurrencyType currencytype);

	int GetSalesPackageId();

	boolean IsFree();

	void Update(GameSalesPackageToken token);
}
