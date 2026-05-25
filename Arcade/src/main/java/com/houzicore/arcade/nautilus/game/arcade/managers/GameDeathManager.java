package com.houzicore.arcade.nautilus.game.arcade.managers;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.game.Game;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.houzicore.shared.common.util.effect.AnimatedExplosion;

/**
 * Centralized Death Manager for Paper 1.21+ compatibility.
 * 
 * <h2>Golden Rule</h2>
 * <b>NEVER mutate a player's state during PlayerDeathEvent.</b>
 * Let Paper's DO_IMMEDIATE_RESPAWN handle the respawn natively,
 * then interact with the player only AFTER PlayerRespawnEvent + delay.
 * 
 * <h2>How it works</h2>
 * <ol>
 *   <li>Player takes lethal damage → vanilla death</li>
 *   <li>PlayerDeathEvent fires → game logic sets OUT, records stats (no player mutations)</li>
 *   <li>DO_IMMEDIATE_RESPAWN auto-respawns → PlayerRespawnEvent fires</li>
 *   <li>GamePlayerManager.PlayerRespawn sets respawn location</li>
 *   <li>This manager waits 5 ticks, then applies spectator mode or position sync</li>
 * </ol>
 * 
 * <h2>Legacy Note</h2>
 * This replaces the legacy 2015-era pattern of calling setGameMode(SPECTATOR)
 * inside PlayerDeathEvent handlers, which is unsafe in Paper 1.21+ because
 * the client receives conflicting dimension-change and respawn packets.
 */
public class GameDeathManager implements Listener
{
	private ArcadeManager Manager;
	
	public GameDeathManager(ArcadeManager manager)
	{
		Manager = manager;
		Manager.getPluginManager().registerEvents(this, Manager.getPlugin());
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		Player player = event.getEntity();
		Game game = Manager.GetGame();

		if (game == null || game.GetState() != GameState.Live)
			return;

		// --- VFX Upgrade: Death Effect Explosion ---
		// Spawn fake redstone blocks exploding outward
		AnimatedExplosion.createFake(player.getLocation(), Material.REDSTONE_BLOCK, 15, 1.5)
			.withSound(Sound.ENTITY_GENERIC_EXPLODE)
			.ignite(Manager.getPlugin());
	}

	/**
	 * Post-respawn safety net.
	 * After Paper completes the respawn cycle, we wait 5 ticks for the client
	 * to fully sync, then apply spectator mode if the player is eliminated.
	 * 
	 * This runs at MONITOR priority so all other respawn handlers
	 * (GamePlayerManager) have already set the respawn location.
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPostRespawn(PlayerRespawnEvent event)
	{
		Player player = event.getPlayer();
		Game game = Manager.GetGame();
		
		// Apply spectator mode during Live AND End states.
		// EndCheck can flip the state to End before the last-killed player's
		// respawn event fires (race condition with DO_IMMEDIATE_RESPAWN),
		// so we must still handle it during End to avoid ghost survivors.
		if (game == null || (game.GetState() != GameState.Live && game.GetState() != GameState.End))
			return;
		
		// Only handle eliminated players who need to become spectators
		// Alive players (respawning in-game) are handled by GamePlayerManager
		if (game.IsAlive(player))
			return;
		
		// Player is OUT — schedule spectator transition after 5 ticks
		Manager.getPlugin().getServer().getScheduler().runTaskLater(Manager.getPlugin(), () -> {
			if (!player.isOnline() || player.isDead())
				return;
			
			Game currentGame = Manager.GetGame();
			if (currentGame == null)
				return;
			
			// Don't double-apply if already spectator
			if (player.hasMetadata("spectator") || player.getGameMode() == GameMode.SPECTATOR)
				return;
			
			// Apply spectator mode safely
			Manager.addSpectator(player, currentGame.GetSpectatorLocation());
			
			// Position sync safety net — force the client to re-sync 2 ticks later
			Manager.getPlugin().getServer().getScheduler().runTaskLater(Manager.getPlugin(), () -> {
				if (player.isOnline() && !player.isDead())
				{
					player.teleport(player.getLocation()); // Force position re-sync
				}
			}, 2L);
			
		}, 5L);
	}
}
