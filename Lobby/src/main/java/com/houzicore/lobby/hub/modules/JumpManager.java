package com.houzicore.lobby.hub.modules;

import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.util.UtilAction;
import com.houzicore.shared.common.util.UtilBlock;
import com.houzicore.shared.common.util.UtilEnt;
import com.houzicore.shared.common.util.UtilParticle;
import com.houzicore.shared.common.util.UtilParticle.ParticleType;
import com.houzicore.shared.common.util.UtilParticle.ViewDist;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.actionbar.ActionBarChannel;
import com.houzicore.shared.common.actionbar.ActionBarService;
import com.houzicore.shared.api.disguise.DisguiseSession;
import java.util.Set;
import com.houzicore.shared.recharge.Recharge;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.lobby.hub.HubManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.HashSet;
import java.util.UUID;

/**
 * JumpManager — Wuxia-style "Light Step" (轻功) double jump for the Lobby.
 *
 * Premium feel:
 *  - Cloud burst + END_ROD particles on launch
 *  - Layered wind/chime sounds
 *  - Mystical trail while airborne
 *  - Qi-charge ActionBar indicator
 *  - Fall damage cancelled after jump
 *  - 1.5s cooldown via Recharge
 */
public class JumpManager extends MiniPlugin
{
	public HubManager Manager;

	// Track who is mid-air from a double jump (for trail + fall cancel)
	private final HashSet<UUID> _airborne = new HashSet<>();

	private static final java.util.Set<String> FLYING_MOB_VARIANTS = java.util.Set.of(
			"CHICKEN", "BAT", "ENDERMAN", "WITHER");

	private static final String RECHARGE_KEY = "LightStep";
	private static final long COOLDOWN_MS = 1500;

	// ActionBar strings
	private static final String QI_READY    = "§a§l⚡ §f§lᴏ̨ɪ ᴘʀᴇᴘᴀʀᴇᴅ §a§l⚡";
	private static final String QI_COOLDOWN = "§8§l⚡ §7ɢᴀᴛʜᴇʀɪɴɢ ǫɪ... §8§l⚡";

	public JumpManager(HubManager manager)
	{
		super("Double Jump", manager.getPlugin());
		Manager = manager;
	}

	// ── Launch ───────────────────────────────────────────────────────────

	@EventHandler
	public void FlightHop(PlayerToggleFlightEvent event)
	{
		Player player = event.getPlayer();
		if (player.getGameMode() == org.bukkit.GameMode.CREATIVE || player.getGameMode() == org.bukkit.GameMode.SPECTATOR) return;

		com.houzicore.shared.api.feature.FeatureGate gate = com.houzicore.lobby.hub.bootstrap.LobbyBootstrap.getInstance().getFeatureGate();
		if (gate != null && !gate.isAllowed(player, com.houzicore.shared.api.feature.FeatureKey.DOUBLE_JUMP))
			return;

		// Disguise bypass (flying mobs) — T-E00: session-based variantKey check
		java.util.Optional<DisguiseSession> activeSession =
				Manager.GetDisguise().getService().getActiveSession(player.getUniqueId());
		if (activeSession.isPresent() && FLYING_MOB_VARIANTS.contains(
				activeSession.get().request().variantKey().toUpperCase()))
			return;

		event.setCancelled(true);
		player.setFlying(false);
		player.setAllowFlight(false);

		// Cooldown check
		if (!Recharge.Instance.use(player, RECHARGE_KEY, COOLDOWN_MS, false, false))
		{
			// Show cooldown ActionBar
			ActionBarService.display(player, ActionBarChannel.GAME_EVENT, LegacyComponentSerializer.legacySection().deserialize(QI_COOLDOWN));
			player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.5f);
			return;
		}

		// ── Velocity (slightly floatier, wuxia feel) ──
		UtilAction.velocity(player, 1.2, 0.35, 1.2, true);

		// ── Sound layering (3 layers, staggered) ──
		// Layer 1: immediate wind burst
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.6f, 1.6f);
		
		// Layer 2: chime (+1 tick)
		org.bukkit.Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
			player.getWorld().playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.5f, 1.8f);
		}, 1L);
		
		// Layer 3: whoosh (+3 ticks)
		org.bukkit.Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BREEZE_WIND_BURST, 0.4f, 1.4f);
		}, 3L);

		// ── Particle burst (cloud + end rod starburst) ──
		UtilParticle.PlayParticle(ParticleType.CLOUD, 
			player.getLocation().add(0, 0.2, 0), 
			0.4F, 0.1F, 0.4F, 0.08F, 25,
			ViewDist.NORMAL, UtilServer.getPlayers());

		UtilParticle.PlayParticle(ParticleType.END_ROD, 
			player.getLocation().add(0, 0.5, 0), 
			0.3F, 0.3F, 0.3F, 0.05F, 12,
			ViewDist.NORMAL, UtilServer.getPlayers());

		// Track airborne state
		_airborne.add(player.getUniqueId());

		// ActionBar
		ActionBarService.display(player, ActionBarChannel.GAME_EVENT, LegacyComponentSerializer.legacySection().deserialize(
			"§b§l✦ §f§lʟɪɢʜᴛ sᴛᴇᴘ §b§l✦"));
	}

	// ── Ground check + re-enable + qi trail ─────────────────────────────

	@EventHandler
	public void FlightUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : UtilServer.getPlayers())
		{
			if (player.getGameMode() == org.bukkit.GameMode.CREATIVE || player.getGameMode() == org.bukkit.GameMode.SPECTATOR) { player.setAllowFlight(true); continue; }
			com.houzicore.shared.api.feature.FeatureGate gate = com.houzicore.lobby.hub.bootstrap.LobbyBootstrap.getInstance().getFeatureGate();
			
			// Context/FeatureGate disables double jump entirely
			if (gate != null && !gate.isAllowed(player, com.houzicore.shared.api.feature.FeatureKey.DOUBLE_JUMP))
			{
				player.setAllowFlight(false);
				player.setFlying(false);
				_airborne.remove(player.getUniqueId());
				continue;
			}

			boolean grounded = UtilEnt.isGrounded(player) 
				|| UtilBlock.solid(player.getLocation().getBlock().getRelative(BlockFace.DOWN));

			if (grounded)
			{
				player.setAllowFlight(true);
				player.setFlying(false);

				// Landing: clear airborne, show qi ready
				if (_airborne.remove(player.getUniqueId()))
				{
					// Soft landing puff
					UtilParticle.PlayParticle(ParticleType.CLOUD, 
						player.getLocation(), 
						0.3F, 0.05F, 0.3F, 0.02F, 8,
						ViewDist.NORMAL, UtilServer.getPlayers());

					player.playSound(player.getLocation(), Sound.BLOCK_WOOL_PLACE, 0.6f, 0.8f);
					ActionBarService.display(player, ActionBarChannel.TOOL_HINT, LegacyComponentSerializer.legacySection().deserialize(QI_READY));
				}
			}
			else if (_airborne.contains(player.getUniqueId()))
			{
				// Airborne trail: mystical wisps every other tick
				if (player.getTicksLived() % 2 == 0)
				{
					UtilParticle.PlayParticle(ParticleType.WITCH_MAGIC, 
						player.getLocation().add(0, 0.5, 0), 
						0.15F, 0.2F, 0.15F, 0.02F, 3,
						ViewDist.NORMAL, UtilServer.getPlayers());
				}
			}
		}
	}

	// ── Fall damage cancel ──────────────────────────────────────────────

	@EventHandler(priority = EventPriority.HIGH)
	public void cancelFallDamage(EntityDamageEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;

		if (event.getCause() != EntityDamageEvent.DamageCause.FALL)
			return;

		Player player = (Player) event.getEntity();

		// Cancel fall damage if they just used the jump (airborne or recently landed)
		if (_airborne.contains(player.getUniqueId()))
		{
			event.setCancelled(true);
			_airborne.remove(player.getUniqueId());
		}
	}
}
