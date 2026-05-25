package com.houzicore.shared.core.hologram;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class HologramManager implements Listener {
	private final JavaPlugin _plugin;
	private final List<Hologram> _activeHolograms = new ArrayList<>();

	public HologramManager(JavaPlugin arcadeManager) {
		_plugin = arcadeManager;
		Bukkit.getPluginManager().registerEvents(this, arcadeManager);
	}

	public JavaPlugin getPlugin() {
		return _plugin;
	}


	void addHologram(Hologram hologram) {
		_activeHolograms.add(hologram);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onTick(UpdateEvent event) {
		if (event.getType() != UpdateType.TICK || _activeHolograms.isEmpty())
			return;
		final List<World> worlds = Bukkit.getWorlds();
		for (final Hologram hologram : new ArrayList<>(_activeHolograms)) {
			if (!worlds.contains(hologram.getLocation().getWorld())) {

				hologram.stop();
				continue;
			}

			if (hologram.getEntityFollowing() != null) {
				final Entity following = hologram.getEntityFollowing();
				if (hologram.isRemoveOnEntityDeath() && !following.isValid()) {
					hologram.stop();
					continue;
				}
				
				hologram.setLocation(following.getLocation().add(hologram.relativeToEntity));
			}

			// Handle visibility
			if (hologram.isInUse()) {
				for (Player player : hologram.getLocation().getWorld().getPlayers()) {
					if (hologram.isVisible(player)) {
						player.showEntity(hologram.getPlugin(), hologram.getDisplayEntity());
					} else {
						player.hideEntity(hologram.getPlugin(), hologram.getDisplayEntity());
					}
				}
			}
		}

	}

	void removeHologram(Hologram hologram) {
		_activeHolograms.remove(hologram);
	}

	public void clearOwner(com.houzicore.shared.core.lifecycle.LifecycleOwner owner) {
		if (owner == null) return;
		for (Hologram hologram : new ArrayList<>(_activeHolograms)) {
			if (hologram.getOwner() == owner) {
				hologram.stop();
			}
		}
	}

	public void removeAll() {
		for (Hologram hologram : new ArrayList<>(_activeHolograms)) {
			hologram.stop();
		}
		_activeHolograms.clear();
	}
}
