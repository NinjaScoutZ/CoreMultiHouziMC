package com.houzicore.shared.core.gadget.types;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.houzicore.shared.core.gadget.GadgetManager;

public abstract class DoubleJumpGadget extends Gadget {

	public DoubleJumpGadget(GadgetManager manager, String name, String[] desc, int cost, Material mat, byte data) {
		super(manager, GadgetType.DoubleJump, name, desc, cost, mat, data);
	}

	@Override
	public void DisableCustom(Player player) {
	}

	@Override
	public void EnableCustom(Player player) {
	}

	public abstract void doDoubleJumpEffect(Player player);
}
