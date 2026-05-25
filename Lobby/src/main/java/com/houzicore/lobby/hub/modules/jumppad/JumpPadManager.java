package com.houzicore.lobby.hub.modules.jumppad;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;

import com.houzicore.lobby.hub.HubManager;
import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

/**
 * Handles launching players who step on Slime Blocks in the Lobby.
 */
public class JumpPadManager extends MiniPlugin {

	public HubManager Manager;
	private final Set<UUID> _airborne = new HashSet<>();

	public JumpPadManager(HubManager manager) {
		super("Jump Pad", manager.getPlugin());
		Manager = manager;
		
		addCommand(new DoubleJumpSetupCommand(this));
	}

	@EventHandler
	public void onLaunch(UpdateEvent event) {
		if (event.getType() != UpdateType.TICK) return;

		for (Player player : UtilServer.getPlayers()) {
			if (player.getGameMode() == GameMode.SPECTATOR) continue;

			boolean grounded = UtilEnt.isGrounded(player) 
				|| com.houzicore.shared.common.util.UtilBlock.solid(player.getLocation().getBlock().getRelative(BlockFace.DOWN));

			if (!grounded) continue;
			
			// Remove from airborne if grounded and not stepping on pad
			Block under = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
			if (under.getType() != Material.SLIME_BLOCK) {
				_airborne.remove(player.getUniqueId());
				continue;
			}
			
			// If stepping on Slime Block, LAUNCH!
			com.houzicore.shared.api.feature.FeatureGate gate = com.houzicore.lobby.hub.bootstrap.LobbyBootstrap.getInstance().getFeatureGate();
			if (gate != null && !gate.isAllowed(player, com.houzicore.shared.api.feature.FeatureKey.JUMP_PAD)) {
				continue;
			}

			UtilAction.velocity(player, player.getLocation().getDirection(), 2.3, true, 1.4, 0, 1.5, true);
			
			// Premium Jump Sound
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SLIME_JUMP, 1.5f, 0.5f);
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0f, 1.2f);
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.0f);
			
			UtilParticle.PlayParticle(ParticleType.CLOUD, 
					player.getLocation().add(0, 0.5, 0), 0.5F, 0.2F, 0.5F, 0.1F, 20,
					ViewDist.NORMAL, UtilServer.getPlayers());
			
			_airborne.add(player.getUniqueId());
		}
	}

	@EventHandler
	public void onParticle(UpdateEvent event) {
		if (event.getType() != UpdateType.SEC) return;

		// Play particles on slime blocks near players, without needing heavy global map scan
		for (Player p : UtilServer.getPlayers()) {
			Location loc = p.getLocation();
			for (int x = -15; x <= 15; x++) {
				for (int y = -5; y <= 5; y++) {
					for (int z = -15; z <= 15; z++) {
						Block b = loc.getBlock().getRelative(x, y, z);
						if (b.getType() == Material.SLIME_BLOCK) {
							// Magical Hop Pad Effect
							UtilParticle.PlayParticle(ParticleType.WITCH_MAGIC, b.getLocation().add(0.5, 1.1, 0.5), 0.4f, 0.2f, 0.4f, 0.05f, 3, ViewDist.NORMAL, UtilServer.getPlayers());
							UtilParticle.PlayParticle(ParticleType.SLIME, b.getLocation().add(0.5, 1.1, 0.5), 0.3f, 0.1f, 0.3f, 0.05f, 2, ViewDist.NORMAL, UtilServer.getPlayers());
						}
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void cancelFallDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)) return;
		if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;

		Player player = (Player) event.getEntity();

		if (_airborne.contains(player.getUniqueId())) {
			event.setCancelled(true);
			_airborne.remove(player.getUniqueId());
			
			// Optional Landing particle
			UtilParticle.PlayParticle(ParticleType.SLIME, player.getLocation(), 0.5F, 0.1F, 0.5F, 0.05F, 10, ViewDist.NORMAL, UtilServer.getPlayers());
			player.playSound(player.getLocation(), Sound.ENTITY_SLIME_SQUISH, 0.8f, 1.0f);
		}
	}
}
