package com.houzicore.shared.core.gadget.gadgets;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.*;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.core.disguise.disguises.DisguiseCow;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.MorphGadget;

public class MorphCow extends MorphGadget {
	public MorphCow(GadgetManager manager) {
		super(manager, "Cow Morph",
				new String[] { C.cWhite + "How now brown cow?", " ",
						C.cYellow + "Left Click" + C.cGray + " to use " + C.cGreen + "Moo", },
				6000, Material.LEATHER, (byte) 0);
	}

	@EventHandler
	public void Audio(PlayerInteractEvent event) {
		final Player player = event.getPlayer();

		if (!IsActive(player))
			return;

		if (!UtilEvent.isAction(event, ActionType.L))
			return;

		if (!Recharge.Instance.use(player, GetName(), 2500, false, false))
			return;

		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_COW_AMBIENT, 1f, 1f);

	}

	@Override
	public void DisableCustom(Player player) {
		RemoveArmor(player);
		Manager.getDisguiseManager().undisguise(player);
	}

	@Override
	public void EnableCustom(final Player player) {
		ApplyArmor(player);

		final DisguiseCow disguise = new DisguiseCow(player);
		//disguise.setName(player.getName(), Manager.getClientManager().Get(player).GetRank());
		//disguise.setCustomNameVisible(true);
		Manager.getDisguiseManager().disguise(disguise);
	}
}
