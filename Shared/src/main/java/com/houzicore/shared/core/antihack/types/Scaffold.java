package com.houzicore.shared.core.antihack.types;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.core.antihack.AntiHack;
import com.houzicore.shared.core.antihack.Detector;

public class Scaffold extends MiniPlugin implements Detector {
	private final AntiHack Host;

	public Scaffold(AntiHack host) {
		super("Scaffold Detector", host.getPlugin());
		Host = host;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlace(BlockPlaceEvent event) {
		if (!Host.isEnabled()) return;
		
		Player player = event.getPlayer();
		
		// If placing a block roughly underneath themselves
		if (event.getBlockPlaced().getY() < player.getLocation().getY()) {
			// Scaffold modules often forget to update pitch visibly, so they place blocks beneath them
			// while looking straight forward or up (pitch < 45, where 90 is straight down).
			float pitch = player.getLocation().getPitch();
			if (pitch < 45.0f && pitch > -90.0f) {
				Host.addSuspicion(player, "Scaffold");
			}
		}
	}

	@Override
	public void Reset(Player player) {
		// Stateless
	}
}
