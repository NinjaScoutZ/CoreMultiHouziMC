package com.houzicore.gateway.listener;

import com.houzicore.gateway.GatewayPlugin;
import com.houzicore.gateway.auth.AuthSession;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

import java.util.Set;

/**
 * Blocks all meaningful player actions until authentication is complete.
 *
 * Allowed before auth:
 *   /login, /register, /2fa  (commands)
 *   Head rotation only (movement)
 *
 * Blocked before auth:
 *   Chat, all other commands, movement, interaction, damage, block edit
 */
public class AuthGuard implements Listener {

    private static final Set<String> ALLOWED_COMMANDS = Set.of("login", "register", "2fa", "hzgate-submit");

    private final GatewayPlugin plugin;

    public AuthGuard(GatewayPlugin plugin) {
        this.plugin = plugin;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private boolean needsGuard(Player player) {
        if (player.hasPermission("houzigate.bypass")) return false;
        AuthSession session = plugin.getSessionManager().get(player);
        return session != null && !session.isAuthenticated();
    }

    // -----------------------------------------------------------------------
    // Movement — allow head rotation only, block all position change
    // -----------------------------------------------------------------------

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMove(PlayerMoveEvent event) {
        if (!needsGuard(event.getPlayer())) return;

        Location from = event.getFrom();
        Location to   = event.getTo();
        if (to == null) return;

        // Block any X/Y/Z position change — only allow yaw/pitch (head rotation)
        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
            Location blocked = from.clone();
            blocked.setYaw(to.getYaw());
            blocked.setPitch(to.getPitch());
            event.setTo(blocked);
        }
    }

    // -----------------------------------------------------------------------
    // Chat
    // -----------------------------------------------------------------------

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!needsGuard(event.getPlayer())) return;
        event.setCancelled(true);
        event.getPlayer().sendMessage(plugin.getGateConfig().prefix() + "§cPlease authenticate first.");
    }

    // -----------------------------------------------------------------------
    // Commands
    // -----------------------------------------------------------------------

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!needsGuard(event.getPlayer())) return;

        String raw = event.getMessage().toLowerCase(); // e.g. "/login foo"
        // Strip leading slash and get first token
        String cmd = raw.replaceFirst("^/", "").split("\\s+")[0];

        if (!ALLOWED_COMMANDS.contains(cmd)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(plugin.getGateConfig().prefix() + "§cAuthenticate first.");
        }
    }

    // -----------------------------------------------------------------------
    // Interactions
    // -----------------------------------------------------------------------

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (!needsGuard(event.getPlayer())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!needsGuard(event.getPlayer())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventory(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!needsGuard(player)) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        if (!needsGuard(event.getPlayer())) return;
        event.setCancelled(true);
    }

    // -----------------------------------------------------------------------
    // Damage — block ALL damage to unauthenticated players
    // -----------------------------------------------------------------------

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && needsGuard(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player && needsGuard(player)) {
            event.setCancelled(true);
        }
        if (event.getEntity() instanceof Player player && needsGuard(player)) {
            event.setCancelled(true);
        }
    }

    // -----------------------------------------------------------------------
    // Block edits
    // -----------------------------------------------------------------------

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (!needsGuard(event.getPlayer())) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (!needsGuard(event.getPlayer())) return;
        event.setCancelled(true);
    }
}
