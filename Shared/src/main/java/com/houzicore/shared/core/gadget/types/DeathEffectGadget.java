package com.houzicore.shared.core.gadget.types;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import com.houzicore.shared.core.gadget.GadgetManager;

public abstract class DeathEffectGadget extends Gadget {
	public DeathEffectGadget(GadgetManager manager, String name, String[] desc, int cost, Material mat, byte data) {
		super(manager, GadgetType.DeathEffect, name, desc, cost, mat, data);
	}

	@Override
	public void EnableCustom(Player player) { }

	@Override
	public void DisableCustom(Player player) { }

	// Triggered by Arcade games when a player dies
	public abstract void PlayEffect(Player player);
}
