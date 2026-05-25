package com.houzicore.shared.core.gadget.types;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.gadget.GadgetManager;

public abstract class MorphGadget extends Gadget {
	public MorphGadget(GadgetManager manager, String name, String[] desc, int cost, Material mat, byte data) {
		super(manager, GadgetType.Morph, name, desc, cost, mat, data);
	}

	public MorphGadget(GadgetManager manager, String name, String[] desc, int cost) {
		super(manager, GadgetType.Morph, name, desc, cost, Material.AIR, (byte) 0);
	}

	public void ApplyArmor(Player player) {
		Manager.RemoveMorph(player);

		_active.add(player);

		UtilPlayer.message(player, F.main("Gadget", com.houzicore.shared.core.lang.LangManager.get().get(player, "gadget.morphed").replace("{0}", F.elem(GetName()))));
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent event) {
		Disable(event.getEntity());
	}

	public void RemoveArmor(Player player) {
		if (_active.remove(player)) {
			UtilPlayer.message(player, F.main("Gadget", com.houzicore.shared.core.lang.LangManager.get().get(player, "gadget.unmorphed").replace("{0}", F.elem(GetName()))));
		}
	}
}
