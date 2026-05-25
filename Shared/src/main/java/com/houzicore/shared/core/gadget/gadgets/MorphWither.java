package com.houzicore.shared.core.gadget.gadgets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.util.Vector;

import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilAlg;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.core.disguise.disguises.DisguiseBase;
import com.houzicore.shared.core.disguise.disguises.DisguiseWither;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.shared.core.gadget.GadgetManager;
import com.houzicore.shared.core.gadget.types.MorphGadget;

public class MorphWither extends MorphGadget {
	private final ArrayList<WitherSkull> _skulls = new ArrayList<>();

	public MorphWither(GadgetManager manager) {
		super(manager, "Wither Morph",
				new String[] { C.cWhite + "Become a legendary Wither!", " ",
						C.cYellow + "Left Click" + C.cGray + " to use " + C.cGreen + "Wither Skull", " ",
						C.cPurple + "Unlocked with Legend Rank", },
				30000, Material.PLAYER_HEAD, (byte) 1);
	}

	@EventHandler
	public void clean(UpdateEvent event) {
		if (event.getType() != UpdateType.FAST)
			return;

		final Iterator<WitherSkull> skullIterator = _skulls.iterator();

		while (skullIterator.hasNext()) {
			final WitherSkull skull = skullIterator.next();

			if (!skull.isValid()) {
				skullIterator.remove();
				skull.remove();
				continue;
			}
		}
	}

	@Override
	public void DisableCustom(Player player) {
		RemoveArmor(player);
		Manager.getDisguiseManager().undisguise(player);

		player.setAllowFlight(false);
		player.setFlying(false);

		player.setMaxHealth(20);
		player.setHealth(20);
	}

	@Override
	public void EnableCustom(final Player player) {
		ApplyArmor(player);

		player.setMaxHealth(300);
		player.setHealth(300);

		final DisguiseWither disguise = new DisguiseWither(player);
		//disguise.setName(player.getName(), Manager.getClientManager().Get(player).GetRank());
		// disguise.setCustomNameVisible(true);
		Manager.getDisguiseManager().disguise(disguise);

		player.setMaxHealth(20);
		player.setHealth(20);
	}

	@EventHandler
	public void explode(EntityExplodeEvent event) {
		if (!_skulls.contains(event.getEntity()))
			return;

		event.setCancelled(true);

		final WitherSkull skull = (WitherSkull) event.getEntity();

		UtilParticle.PlayParticle(ParticleType.LARGE_EXPLODE, skull.getLocation(), 0, 0, 0, 0, 1, ViewDist.MAX,
				UtilServer.getPlayers());
		skull.getWorld().playSound(skull.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2f, 1f);

		final HashMap<Player, Double> players = UtilPlayer.getInRadius(event.getLocation(), 6);
		for (final Player player : players.keySet()) {
			if (Manager.collideEvent(this, player)) {
				continue;
			}

			final double mult = players.get(player);

			// Knockback
			UtilAction.velocity(player, UtilAlg.getTrajectory(event.getLocation(), player.getLocation()), 2 * mult,
					false, 0, 0.6 + 0.4 * mult, 2, true);
		}
	}

	@EventHandler
	public void flight(UpdateEvent event) {
		if (event.getType() != UpdateType.TICK)
			return;

		for (final Player player : GetActive()) {
			if (UtilPlayer.isSpectator(player)) {
				continue;
			}

			player.setAllowFlight(true);
			player.setFlying(true);

			if (UtilEnt.isGrounded(player)) {
				player.setVelocity(new Vector(0, 1, 0));
			}
		}
	}

	@EventHandler
	public void legendOwner(PlayerJoinEvent event) {
		if (Manager.getClientManager().Get(event.getPlayer()).GetRank() == Rank.DIVINE
				|| Manager.getClientManager().Get(event.getPlayer()).GetRank() == Rank.ADMIN
				|| Manager.getClientManager().Get(event.getPlayer()).GetRank() == Rank.DEVELOPER
				|| Manager.getClientManager().Get(event.getPlayer()).GetRank() == Rank.OWNER) {
			Manager.getDonationManager().Get(event.getPlayer().getName()).AddUnknownSalesPackagesOwned(GetName());
		}
	}

	public void setWitherData(String text, double healthPercent) {
		final Iterator<Player> activeIterator = GetActive().iterator();

		while (activeIterator.hasNext()) {
			final Player player = activeIterator.next();

			final DisguiseBase disguise = Manager.getDisguiseManager().getDisguise(player);

			if (disguise == null || !(disguise instanceof DisguiseWither)) {
				DisableCustom(player);
				activeIterator.remove();
				continue;
			}

			((DisguiseWither) disguise).setName(text);
			((DisguiseWither) disguise).setHealth((float) (healthPercent * 300));
			Manager.getDisguiseManager().updateDisguise(disguise);
		}
	}

	@EventHandler
	public void witherSkull(PlayerInteractEvent event) {
		final Player player = event.getPlayer();

		if (!IsActive(player))
			return;

		if (!UtilEvent.isAction(event, ActionType.L))
			return;

		if (!Recharge.Instance.use(player, GetName(), 1600, false, false))
			return;

		final Vector offset = player.getLocation().getDirection();
		if (offset.getY() < 0) {
			offset.setY(0);
		}

		_skulls.add(player.launchProjectile(WitherSkull.class));

		// Effect
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.5f, 1f);
	}
}
