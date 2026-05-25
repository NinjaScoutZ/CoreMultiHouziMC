package com.houzicore.shared.core.gadget.gadgets;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.core.gadget.types.ParticleGadget;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.gadget.GadgetManager;

public class ParticleHeart extends ParticleGadget {
	private final HashMap<Player, HashMap<Player, Location>> _target = new HashMap<>();

	public ParticleHeart(GadgetManager manager) {
		super(manager, "I Heart You", new String[] { C.cWhite + "With these particles, you can",
				C.cWhite + "show off how much you heart", C.cWhite + "everyone on " + com.houzicore.shared.core.common.BrandConfig.mainServerName() + "!", }, -2, Material.APPLE,
				(byte) 0);
	}

	private void clean(Player player) {
		_target.remove(player);

		for (final HashMap<Player, Location> map : _target.values()) {
			map.remove(player);
		}
	}

	@Override
	public void DisableCustom(Player player) {
		if (_active.remove(player)) {
			UtilPlayer.message(player, F.main("Gadget", "You unsummoned " + F.elem(GetName()) + "."));
		}

		clean(player);
	}

	@EventHandler
	public void playParticle(UpdateEvent event) {
		if (event.getType() != UpdateType.FASTEST)
			return;

		// Launch
		for (final Player player : GetActive()) {
			if (!shouldDisplay(player)) {
				continue;
			}

			if (!_target.containsKey(player)) {
				_target.put(player, new HashMap<Player, Location>());
			}

			if (Recharge.Instance.use(player, GetName(), 500, false, false)) {
				for (final Player other : UtilServer.getPlayers()) {
					if (other.equals(player)) {
						continue;
					}

					if (!UtilPlayer.isSpectator(other)) {
						continue;
					}

					if (_target.get(player).containsKey(other)) {
						continue;
					}

					if (UtilMath.offset(player, other) > 6) {
						continue;
					}

					_target.get(player).put(other, player.getLocation().add(0, 1, 0));

					break;
				}
			}

			if (Manager.isMoving(player)) {
				UtilParticle.PlayParticle(ParticleType.HEART, player.getLocation().add(0, 1, 0), 0f, 0f, 0f, 0, 1,
						ViewDist.NORMAL, UtilServer.getPlayers());
			} else {
				UtilParticle.PlayParticle(ParticleType.HEART, player.getLocation().add(0, 1, 0), 0.5f, 0.5f, 0.5f, 0, 1,
						ViewDist.NORMAL, UtilServer.getPlayers());
			}
		}

		// Particle
		for (final HashMap<Player, Location> heart : _target.values()) {
			final Iterator<Entry<Player, Location>> heartIterator = heart.entrySet().iterator();

			while (heartIterator.hasNext()) {
				final Entry<Player, Location> entry = heartIterator.next();

				entry.getValue()
						.add(UtilAlg.getTrajectory(entry.getValue(), entry.getKey().getEyeLocation()).multiply(0.6));

				UtilParticle.PlayParticle(ParticleType.HEART, entry.getValue(), 0, 0, 0, 0, 1, ViewDist.NORMAL,
						UtilServer.getPlayers());

				if (UtilMath.offset(entry.getValue(), entry.getKey().getEyeLocation()) < 0.6) {
					heartIterator.remove();
				}
			}
		}
	}

	@EventHandler
	public void quit(PlayerQuitEvent event) {
		clean(event.getPlayer());
	}
}
