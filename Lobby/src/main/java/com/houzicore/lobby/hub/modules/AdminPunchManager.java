package com.houzicore.lobby.hub.modules;

import java.util.HashSet;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.Rank;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilEvent;
import com.houzicore.shared.common.util.UtilEvent.ActionType;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.lobby.hub.HubManager;

public class AdminPunchManager extends MiniPlugin {
	public HubManager Manager;
	private HashSet<Player> _active = new HashSet<>();

	public AdminPunchManager(HubManager manager) {
		super("Admin Punch", manager.getPlugin());
		Manager = manager;
	}

	public void togglePunch(Player player) {
		if (_active.contains(player)) {
			_active.remove(player);
			UtilPlayer.message(player, F.main("Mystery", "Rocket Punch: " + F.oo(false)));
		} else {
			_active.add(player);
			UtilPlayer.message(player, F.main("Mystery", "Rocket Punch: " + F.oo(true)));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPunch(PlayerInteractEvent event) {
		if (!UtilEvent.isAction(event, ActionType.L))
			return;

		Player player = event.getPlayer();
		if (!_active.contains(player))
			return;

		// Double check rank to ensure authorization is still valid
		if (!Manager.GetClients().Get(player).GetRank().Has(player, Rank.ADMIN, false)) {
			_active.remove(player);
			return;
		}

		if (!Recharge.Instance.use(player, "Rocket Punch Attack", 500, false, false))
			return;

		for (Player other : UtilPlayer.getNearby(player.getLocation(), 4.0)) {
			if (player.equals(other))
				continue;

			// Don't punch Chiss or Developer
			if (other.getName().equals("Chiss"))
				continue;

			// Launch the target into the sky!
			other.setVelocity(new Vector(0, 2.2, 0));
			
			// Visuals and sounds
			UtilParticle.PlayParticleToAll(ParticleType.HUGE_EXPLOSION, other.getLocation().add(0, 0.5, 0), 0f, 0f, 0f, 0f, 1, ViewDist.LONG);
			UtilParticle.PlayParticleToAll(ParticleType.LAVA, other.getLocation(), 0.5f, 0.5f, 0.5f, 0.1f, 15, ViewDist.NORMAL);
			other.getWorld().playSound(other.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 1.0f);
			
			UtilPlayer.message(other, F.main("Mystery", F.name(player.getName()) + " rocket punched you into the sky!"));
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		_active.remove(event.getPlayer());
	}
}
