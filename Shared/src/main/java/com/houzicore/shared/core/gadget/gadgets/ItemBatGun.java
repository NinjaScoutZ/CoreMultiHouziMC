package com.houzicore.shared.core.gadget.gadgets;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilTime;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.ItemGadget;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class ItemBatGun extends ItemGadget {
	private final HashMap<Player, Long> _active = new HashMap<>();
	private final HashMap<Player, Location> _velocity = new HashMap<>();
	private final HashMap<Player, ArrayList<Bat>> _bats = new HashMap<>();

	public ItemBatGun(GadgetManager manager) {
		super(manager, "Bat Blaster",
				new String[] { C.cWhite + "Launch waves of annoying bats", C.cWhite + "at people you don't like!", },
				-1, Material.IRON_HORSE_ARMOR, (byte) 0, 5000, new Ammo("Bat Blaster", "50 Bats", Material.IRON_HORSE_ARMOR,
						(byte) 0, new String[] { C.cWhite + "50 Bats for your Bat Blaster!" }, 500, 50));
	}

	@Override
	public void ActivateCustom(Player player) {
		// Start
		_velocity.put(player, player.getEyeLocation());
		_active.put(player, System.currentTimeMillis());

		_bats.put(player, new ArrayList<Bat>());

		for (int i = 0; i < 16; i++) {
			_bats.get(player).add(player.getWorld().spawn(player.getEyeLocation(), Bat.class));
		}

		// Inform
		boolean isThai = com.houzicore.shared.core.lang.LangManager.get().isThai(player);
		UtilPlayer.message(player, F.main("Skill", isThai ? "\u00A77\u0e04\u0e38\u0e13\u0e43\u0e0a\u0e49 " + F.skill(GetName()) : "§7You used " + F.skill(GetName())));
	}

	public void Clear(Player player) {
		_active.remove(player);
		_velocity.remove(player);
		if (_bats.containsKey(player)) {
			for (final Bat bat : _bats.get(player)) {
				if (bat.isValid()) {
					UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, bat.getLocation(), 0, 0, 0, 0, 3,
							ViewDist.NORMAL, UtilServer.getPlayers());
				}

				bat.remove();
			}

			_bats.remove(player);
		}
	}

	@Override
	public void DisableCustom(Player player) {
		super.DisableCustom(player);

		Clear(player);
	}

	@EventHandler
	public void Update(UpdateEvent event) {
		if (event.getType() != UpdateType.TICK)
			return;

		for (final Player cur : UtilServer.getPlayers()) {
			if (!_active.containsKey(cur)) {
				continue;
			}

			if (UtilTime.elapsed(_active.get(cur), 3000)) {
				Clear(cur);
				continue;
			}

			final Location loc = _velocity.get(cur);

			// Bat Movement
			for (final Bat bat : _bats.get(cur)) {
				if (!bat.isValid()) {
					continue;
				}
				final Vector rand = new Vector((Math.random() - 0.5) / 3, (Math.random() - 0.5) / 3,
						(Math.random() - 0.5) / 3);
				bat.setVelocity(loc.getDirection().clone().multiply(0.5).add(rand));

				for (final Player other : UtilServer.getPlayers()) {
					if (other.equals(cur)) {
						continue;
					}

					if (!Manager.getPreferencesManager().Get(other).HubGames
							|| !Manager.getPreferencesManager().Get(other).ShowPlayers) {
						continue;
					}

					if (!Recharge.Instance.usable(other, "Hit by Bat")) {
						continue;
					}

					if (UtilEnt.hitBox(bat.getLocation(), other, 2, null)) {
						if (Manager.collideEvent(this, other)) {
							continue;
						}

						// Damage Event
						UtilAction.velocity(other, UtilAlg.getTrajectory(cur, other), 0.4, false, 0, 0.2, 10, true);

						// Effect
						bat.getWorld().playSound(bat.getLocation(), Sound.ENTITY_BAT_HURT, 1f, 1f);
						UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, bat.getLocation(), 0, 0, 0, 0, 3,
								ViewDist.NORMAL, UtilServer.getPlayers());

						bat.remove();

						// Recharge on hit
						Recharge.Instance.useForce(other, "Hit by Bat", 200);
					}
				}
			}
		}
	}
}
