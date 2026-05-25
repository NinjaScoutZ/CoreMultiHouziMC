package com.houzicore.shared.core.gadget.gadgets;
import com.houzicore.shared.core.disguise.disguises.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.core.event.StackerEvent;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.MorphGadget;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class MorphBlock extends MorphGadget {
	private final HashMap<Player, BlockForm> _active = new HashMap<>();

	public MorphBlock(GadgetManager manager) {
		super(manager, "Block Morph",
				new String[] { C.cWhite + "The blockiest block that ever blocked.", " ",
						C.cYellow + "Left Click" + C.cGray + " to use " + C.cGreen + "Change Block",
						C.cYellow + "Stay Still" + C.cGray + " to use " + C.cGreen + "Solidify", },
				30000, Material.EMERALD_BLOCK, (byte) 0);
	}

	@Override
	public void DisableCustom(Player player) {
		RemoveArmor(player);

		final BlockForm form = _active.remove(player);
		if (form != null) {
			form.Remove();
		}
	}

	@Override
	public void EnableCustom(final Player player) {
		ApplyArmor(player);

		_active.put(player, new BlockForm(this, player, Material.EMERALD_BLOCK));
	}

	// Removed unused fallingBlock methods since BlockForm uses native DisguiseAPI

	@EventHandler
	public void formChange(PlayerInteractEvent event) {
		if (event.getClickedBlock() == null)
			return;

		if (!UtilEvent.isAction(event, ActionType.L_BLOCK) && !UtilEvent.isAction(event, ActionType.R_BLOCK))
			return;

		if (!UtilBlock.solid(event.getClickedBlock()))
			return;

		if (!Recharge.Instance.use(event.getPlayer(), GetName(), 500, false, false))
			return;

		final BlockForm form = _active.get(event.getPlayer());

		if (form == null)
			return;

		form.Remove();

		_active.put(event.getPlayer(), new BlockForm(this, event.getPlayer(), event.getClickedBlock().getType()));
	}

	@EventHandler
	public void formUpdate(UpdateEvent event) {
		if (event.getType() != UpdateType.TICK)
			return;

		for (final BlockForm form : _active.values()) {
			form.SolidifyUpdate();
		}
	}

	// Removed itemSpawnCancel because we no longer have real falling block items drop

	@EventHandler
	public void stacker(StackerEvent event) {
		if (_active.containsKey(event.getEntity())) {
			event.setCancelled(true);
		}
	}
}
