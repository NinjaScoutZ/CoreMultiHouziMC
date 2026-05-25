package com.houzicore.shared.core.gadget.types;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.gadget.GadgetManager;

public abstract class ParticleGadget extends Gadget {
	public ParticleGadget(GadgetManager manager, String name, String[] desc, int cost, Material mat, byte data) {
		super(manager, GadgetType.Particle, name, desc, cost, mat, data);
	}

	@Override
	public void DisableCustom(Player player) {
		if (_active.remove(player)) {
			UtilPlayer.message(player, F.main("Gadget", com.houzicore.shared.core.lang.LangManager.get().get(player, "gadget.disabled").replace("{0}", F.elem(GetName()))));
		}
	}

	@Override
	public void EnableCustom(Player player) {
		Manager.RemoveParticle(player);

		_active.add(player);

		UtilPlayer.message(player, F.main("Gadget", com.houzicore.shared.core.lang.LangManager.get().get(player, "gadget.enabled").replace("{0}", F.elem(GetName()))));
	}

	public boolean shouldDisplay(Player player) {
		if (UtilPlayer.isSpectator(player))
			return false;

		if (Manager.hideParticles())
			return false;

		return true;
	}
}
