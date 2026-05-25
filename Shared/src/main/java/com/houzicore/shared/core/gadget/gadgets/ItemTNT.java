package com.houzicore.shared.core.gadget.gadgets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.ItemGadget;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class ItemTNT extends ItemGadget {
	private final HashSet<TNTPrimed> _tnt = new HashSet<>();

	public ItemTNT(GadgetManager manager) {
		super(manager, "TNT", new String[] { C.cWhite + "Blow some people up!", C.cWhite + "KABOOM!", }, 3000,
				Material.TNT, (byte) 0, 1000, new Ammo("TNT", "20 TNT", Material.TNT, (byte) 0,
						new String[] { C.cWhite + "20 TNT for you to explode!" }, 500, 20));
	}

	@Override
	public void ActivateCustom(Player player) {
		final TNTPrimed tnt = player.getWorld().spawn(player.getEyeLocation().add(player.getLocation().getDirection()),
				TNTPrimed.class);
		UtilAction.velocity(tnt, player.getLocation().getDirection(), 0.6, false, 0, 0.2, 1, false);
		_tnt.add(tnt);

		// Inform
		boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
		UtilPlayer.message(player, F.main("Skill", isThai ? "\u00A77\u0e04\u0e38\u0e13\u0e02\u0e27\u0e49\u0e32\u0e07 " + F.skill(GetName()) : "§7You threw " + F.skill(GetName())));
	}

	@EventHandler
	public void Clean(UpdateEvent event) {
		if (event.getType() != UpdateType.FAST)
			return;

		final Iterator<TNTPrimed> tntIterator = _tnt.iterator();

		while (tntIterator.hasNext()) {
			final TNTPrimed tnt = tntIterator.next();

			if (!tnt.isValid() || tnt.getTicksLived() > 200) {
				tnt.remove();
				tntIterator.remove();
			}
		}
	}

	@EventHandler
	public void Update(EntityExplodeEvent event) {
		if (!(event.getEntity() instanceof TNTPrimed))
			return;

		if (!_tnt.remove(event.getEntity()))
			return;

		final HashMap<Player, Double> players = UtilPlayer.getInRadius(event.getLocation(), 10);
		for (final Player player : players.keySet()) {
			if (Manager.collideEvent(this, player)) {
				continue;
			}

			final double mult = players.get(player);

			// Knockback
			UtilAction.velocity(player, UtilAlg.getTrajectory(event.getLocation(), player.getLocation()), 3 * mult,
					false, 0, 0.5 + 2 * mult, 10, true);
		}
	}
}
