package com.houzicore.shared.core.gadget.types;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import com.houzicore.shared.core.gadget.GadgetManager;

public abstract class GameTauntGadget extends Gadget {
	public GameTauntGadget(GadgetManager manager, String name, String[] desc, int cost, Material mat, byte data) {
		super(manager, GadgetType.GameTaunt, name, desc, cost, mat, data);
	}

	@Override
	public void EnableCustom(Player player) { }

	@Override
	public void DisableCustom(Player player) { }

	// Triggered by Arcade games when a player taunts
	public abstract void PlayTaunt(Player player);
}
