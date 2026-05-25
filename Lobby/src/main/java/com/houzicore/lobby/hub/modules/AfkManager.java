package com.houzicore.lobby.hub.modules;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.util.UtilText;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import com.houzicore.lobby.hub.HubManager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import java.time.Duration;

/**
 * AFK Detection & Auto-kick (#20)
 * - Tracks player activity (movement, chat, interact)
 * - Warns at AFK_WARN_MS, kicks at AFK_KICK_MS
 * - Resets on any activity
 */
public class AfkManager implements Listener {

    private static final long AFK_WARN_MS = 3 * 60 * 1000L;  // 3 minutes
    private static final long AFK_KICK_MS = 5 * 60 * 1000L;  // 5 minutes

    private final HubManager _hub;
    private final HashMap<UUID, Long> _lastActivity = new HashMap<>();
    private final java.util.Set<UUID> _warned = new java.util.HashSet<>();

    public AfkManager(HubManager hub) {
        _hub = hub;
        hub.getPlugin().getServer().getPluginManager().registerEvents(this, hub.getPlugin());
    }

    private void resetActivity(Player player) {
        _lastActivity.put(player.getUniqueId(), System.currentTimeMillis());
        _warned.remove(player.getUniqueId());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() != event.getTo().getBlockX()
            || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            resetActivity(event.getPlayer());
        }
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        resetActivity(event.getPlayer());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        resetActivity(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        _lastActivity.remove(event.getPlayer().getUniqueId());
        _warned.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (event.getType() != UpdateType.SEC) return;

        long now = System.currentTimeMillis();

        for (Player player : UtilServer.getPlayers()) {
            UUID id = player.getUniqueId();
            if (!_lastActivity.containsKey(id)) {
                _lastActivity.put(id, now);
                continue;
            }

            long idleMs = now - _lastActivity.get(id);

            if (idleMs >= AFK_KICK_MS) {
                // Kick player
                String kickMsg = com.houzicore.shared.core.lang.LangManager.get().get(player, "afk.kick");
                player.kick(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize(kickMsg));
            } else if (idleMs >= AFK_WARN_MS && !_warned.contains(id)) {
                // Warn player
                _warned.add(id);
                long remaining = (AFK_KICK_MS - idleMs) / 1000;
                Title.Times times = Title.Times.times(Duration.ofMillis(300), Duration.ofMillis(3000), Duration.ofMillis(500));
                
                String titleStr = com.houzicore.shared.core.lang.LangManager.get().get(player, "afk.warning_title");
                String subtitleStr = com.houzicore.shared.core.lang.LangManager.get().get(player, "afk.warning_subtitle").replace("{0}", String.valueOf(remaining));
                String chatMsg = com.houzicore.shared.core.lang.LangManager.get().get(player, "afk.warning_chat").replace("{0}", String.valueOf(remaining));
                chatMsg = org.bukkit.ChatColor.translateAlternateColorCodes('&', chatMsg);
                
                player.showTitle(Title.title(
                    net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize(titleStr),
                    net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize(subtitleStr),
                    times
                ));
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 0.5f);
                player.sendMessage(F.main("AFK", chatMsg));
            }
        }
    }
}
