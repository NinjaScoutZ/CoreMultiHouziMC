package com.houzicore.shared.core.spawn;

import java.util.ArrayList;
import java.util.List;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilMath;
import com.houzicore.shared.common.util.UtilPlayer;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilWorld;
import com.houzicore.shared.core.spawn.command.SpawnCommand;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Spawn extends MiniPlugin {
	private final SpawnRepository _repository;

	private final List<Location> _spawns = new ArrayList<>();

	public Spawn(JavaPlugin plugin, String serverName) {
		super("Spawn", plugin);

		_repository = new SpawnRepository(plugin, serverName);

		for (final String spawn : _repository.retrieveSpawns()) {
			_spawns.add(UtilWorld.strToLoc(spawn));
		}
	}

	@Override
	public void addCommands() {
		addCommand(new SpawnCommand(this));
	}

	public void AddSpawn(Player player) {
		// Set Spawn Point
		final Location loc = player.getLocation();

		// Set World Spawn
		player.getWorld().setSpawnLocation((int) loc.getX(), (int) loc.getY(), (int) loc.getZ());

		// Add Spawn
		_spawns.add(loc);

		// Save
		runAsync(new Runnable() {
			@Override
			public void run() {
				_repository.addSpawn(UtilWorld.locToStr(loc));
			}
		});

		// Inform
		UtilPlayer.message(player, F.main(_moduleName, com.houzicore.shared.core.lang.LangManager.get().get(player, "spawn.added")));

		// Log
		log("Added Spawn [" + UtilWorld.locToStr(loc) + "] by [" + player.getName() + "].");
	}

	public void ClearSpawn(Player player) {
		// Add Spawn
		_spawns.clear();

		// Save
		runAsync(new Runnable() {
			@Override
			public void run() {
				_repository.clearSpawns();
			}
		});

		// Inform
		UtilPlayer.message(player, F.main(_moduleName, com.houzicore.shared.core.lang.LangManager.get().get(player, "spawn.cleared")));

		// Log
		log("Cleared Spawn [ALL] by [" + player.getName() + "].");
	}

	public Location getSpawn() {
		if (_spawns.isEmpty())
			return UtilServer.getServer().getWorld("world").getSpawnLocation();

		return _spawns.get(UtilMath.r(_spawns.size()));
	}

	@EventHandler
	public void handleRespawn(PlayerRespawnEvent event) {
		event.setRespawnLocation(getSpawn());
	}
}
