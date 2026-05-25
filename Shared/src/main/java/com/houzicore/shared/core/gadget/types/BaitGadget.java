package com.houzicore.shared.core.gadget.types;

import org.bukkit.Material;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;

import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.gadgets.Ammo;
import com.houzicore.shared.core.lang.LangManager;

public abstract class BaitGadget extends Gadget {
	private final Ammo _ammo;

	public BaitGadget(GadgetManager manager, String name, String[] desc, int cost, Material mat, byte data, Ammo ammo) {
		super(manager, GadgetType.Bait, name, desc, cost, mat, data);
		_ammo = ammo;
		Free = true;
	}

	@Override
	public void DisableCustom(Player player) {
		RemoveItem(player);
	}

	@Override
	public void EnableCustom(Player player) {
		ApplyItem(player, true);
	}

	public void ApplyItem(Player player, boolean inform) {
		Manager.RemoveBait(player); // Clean other baits if needed, wait, we'll just handle it broadly
        Manager.RemoveItem(player); // Clear active item so they don't have multiple
		_active.add(player);

		if (inform) {
			UtilPlayer.message(player, F.main("Gadget", LangManager.get().get(player, "gadget.equipped").replace("{0}", F.elem(GetName()))));
		}
	}

	public void RemoveItem(Player player) {
		if (_active.remove(player)) {
			UtilPlayer.message(player, F.main("Gadget", LangManager.get().get(player, "gadget.unequipped").replace("{0}", F.elem(GetName()))));
		}
	}

	public Ammo getAmmo() {
		return _ammo;
	}

	public boolean hasAmmo(Player player) {
		return Manager.getInventoryManager().Get(player).getItemCount(GetName()) > 0;
	}

	public abstract void playBobberEffect(FishHook hook);
}
