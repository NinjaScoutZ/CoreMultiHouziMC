package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.OutfitGadget;

public class OutfitSpaceSuit extends OutfitGadget {
	public OutfitSpaceSuit(GadgetManager manager, String name, int cost, ArmorSlot slot, Material mat, byte data) {
		super(manager, name,
				new String[] { "Wear the complete set for", "awesome bonus effects!", "Bonus coming soon..." }, cost,
				slot, mat, data);
	}

	@Override
	public void DisableCustom(Player player) {
		RemoveArmor(player);
	}

	@Override
	public void EnableCustom(Player player) {
		ApplyArmor(player);
	}
}
