package com.houzicore.shared.core.shop.item;

import org.bukkit.Material;

public interface IDisplayPackage {
	String[] GetDescription();

	byte GetDisplayData();

	Material GetDisplayMaterial();

	String GetName();
}
