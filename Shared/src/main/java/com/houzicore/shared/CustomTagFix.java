package com.houzicore.shared;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomTagFix extends MiniPlugin {

	public CustomTagFix(JavaPlugin plugin) {
		super("Custom Tag Fix", plugin);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		player.setCustomName(player.getName());
		player.setCustomNameVisible(false);
	}
}
