package com.houzicore.shared.core.gadget.gadgets;

import java.util.HashMap;

import org.bukkit.EntityEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.core.disguise.disguises.DisguiseBase;
import com.houzicore.shared.core.disguise.disguises.DisguiseCreeper;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.MorphGadget;

public class MorphCreeper extends MorphGadget {
	private final HashMap<Player, Long> _active = new HashMap<>();

	public MorphCreeper(GadgetManager manager) {
		super(manager, "Creeper Morph",
				new String[] { C.cWhite + "Transforms the wearer into a creepy Creeper!", " ",
						C.cYellow + "Crouch" + C.cGray + " to use " + C.cGreen + "Detonate", " ",
						C.cPurple + "Unlocked with Hero Rank", },
				-1, Material.PLAYER_HEAD, (byte) 4);
	}

	@EventHandler
	public void Clean(PlayerQuitEvent event) {
		_active.remove(event.getPlayer());
	}

	public void DecreaseSize(Player player) {
		final DisguiseCreeper creeper = GetDisguise(player);
		if (creeper == null)
			return;

		//creeper.a(-1);

		Manager.getDisguiseManager().updateDisguise(creeper);
	}

	@Override
	public void DisableCustom(Player player) {
		RemoveArmor(player);
		Manager.getDisguiseManager().undisguise(player);
	}

	@Override
	public void EnableCustom(final Player player) {
		ApplyArmor(player);

		final DisguiseCreeper disguise = new DisguiseCreeper(player);
		//disguise.setName(player.getName(), Manager.getClientManager().Get(player).GetRank());
		//disguise.setCustomNameVisible(true);
		Manager.getDisguiseManager().disguise(disguise);
	}

	public DisguiseCreeper GetDisguise(Player player) {
		final DisguiseBase disguise = Manager.getDisguiseManager().getDisguise(player);
		if (disguise == null)
			return null;

		if (!(disguise instanceof DisguiseCreeper))
			return null;

		return (DisguiseCreeper) disguise;
	}

	public int GetSize(Player player) {
		final DisguiseCreeper creeper = GetDisguise(player);
		if (creeper == null)
			return 0;

		return 0; //creeper.bV();
	}

	@EventHandler
	public void HeroOwner(PlayerJoinEvent event) {
		if (Manager.getClientManager().Get(event.getPlayer()).GetRank().Has(Rank.SOVEREIGN)) {
			Manager.getDonationManager().Get(event.getPlayer().getName()).AddUnknownSalesPackagesOwned(GetName());
		}
	}

	public void IncreaseSize(Player player) {
		final DisguiseCreeper creeper = GetDisguise(player);
		if (creeper == null)
			return;

		//creeper.a(1);

		Manager.getDisguiseManager().updateDisguise(creeper);
	}

	@EventHandler
	public void Trigger(UpdateEvent event) {
		if (event.getType() == UpdateType.TICK) {
			for (final Player player : GetActive()) {
				if (player.isSneaking()) {
					player.leaveVehicle();
					player.eject();

					if (!_active.containsKey(player)) {
						_active.put(player, System.currentTimeMillis());
					}

					final double elapsed = (System.currentTimeMillis() - _active.get(player)) / 1000d;

					player.setExp(Math.min(0.99f, (float) (elapsed / 1.5)));

					// Sound
					player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, (float) (0.5 + elapsed / 3d),
							(float) (0.5 + elapsed));

					IncreaseSize(player);
				} else if (_active.containsKey(player)) {
					// Unpower
					DecreaseSize(player);

					player.setExp(0f);

					final double elapsed = (System.currentTimeMillis() - _active.remove(player)) / 1000d;

					if (elapsed < 1.5) {
						continue;
					}

					// Explode
					UtilParticle.PlayParticle(ParticleType.HUGE_EXPLOSION, player.getLocation(), 0, 0.5f, 0, 0, 1,
							ViewDist.MAX, UtilServer.getPlayers());
					player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 0.8f);

					player.playEffect(EntityEffect.HURT);

					// Knockback
					final HashMap<Player, Double> players = UtilPlayer.getInRadius(player.getLocation(), 8);
					for (final Player other : players.keySet()) {
						if (other.equals(player)) {
							continue;
						}

						if (Manager.collideEvent(this, other)) {
							continue;
						}

						final double mult = players.get(other);

						// Knockback
						UtilAction.velocity(other, UtilAlg.getTrajectory(player.getLocation(), other.getLocation()),
								1 + 1.5 * mult, false, 0, 0.5 + 1 * mult, 3, true);
					}
				}
			}
		}
	}
}
