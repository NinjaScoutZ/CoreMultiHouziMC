package com.houzicore.gateway.screen;

import com.houzicore.gateway.GatewayPlugin;
import com.houzicore.gateway.auth.AuthSession;
import com.houzicore.gateway.auth.AuthSession.State;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Anvil GUI login screen for HouziGate.
 *
 * Opens an anvil inventory where the player types their password
 * in the rename field. Clicking the output slot submits.
 *
 * For /register: opens twice (password → confirm password).
 * For /2fa: opens once with PIN prompt.
 */
public class AnvilLoginUI implements Listener {

    public enum Mode { LOGIN, REGISTER_PASS1, REGISTER_PASS2, TWO_FA }

    private final GatewayPlugin plugin;

    // Tracks which mode each open anvil is in
    private final Map<UUID, Mode>   playerMode    = new ConcurrentHashMap<>();
    // For register, stores first password between the two anvil opens
    private final Map<UUID, String> pendingPass   = new ConcurrentHashMap<>();

    public AnvilLoginUI(GatewayPlugin plugin) {
        this.plugin = plugin;
    }

    // -----------------------------------------------------------------------
    // Open the UI
    // -----------------------------------------------------------------------

    /** Call on player join to open the correct UI based on session state. */
    public void openFor(Player player) {
        AuthSession session = plugin.getSessionManager().get(player);
        if (session == null || session.isAuthenticated()) return;

        Mode mode = switch (session.getState()) {
            case WAITING_REGISTER -> Mode.REGISTER_PASS1;
            case WAITING_2FA      -> Mode.TWO_FA;
            default               -> Mode.LOGIN;
        };
        openAnvil(player, mode);
    }

    private void openAnvil(Player player, Mode mode) {
        playerMode.put(player.getUniqueId(), mode);

        // Build the prompt item — display name is the hint text
        ItemStack prompt = new ItemStack(Material.PAPER);
        ItemMeta meta = prompt.getItemMeta();
        meta.displayName(Component.empty()); // empty so player can type freely
        meta.lore(java.util.List.of(promptLore(mode)));
        prompt.setItemMeta(meta);

        // Title shown at the top of the anvil UI
        Component title = anvilTitle(mode);

        // Must open on next tick to avoid inventory flicker on join
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;
                @SuppressWarnings("deprecation")
                Inventory inv = Bukkit.createInventory(null, InventoryType.ANVIL, title);
                inv.setItem(0, prompt);
                player.openInventory(inv);
            }
        }.runTaskLater(plugin, 2L);
    }

    // -----------------------------------------------------------------------
    // Inventory events
    // -----------------------------------------------------------------------

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getInventory() instanceof AnvilInventory anvil)) return;

        event.setCancelled(true); // always cancel — we handle it manually

        // Only act on clicking the OUTPUT slot (slot 2)
        if (event.getRawSlot() != 2) return;

        String typed = anvil.getRenameText();
        if (typed == null || typed.isEmpty()) {
            player.sendMessage(plugin.getGateConfig().prefix() + "§cPlease type something first.");
            return;
        }

        Mode mode = playerMode.get(player.getUniqueId());
        if (mode == null) return;

        player.closeInventory();
        handleSubmit(player, mode, typed);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!(event.getInventory() instanceof AnvilInventory)) return;

        UUID uid = player.getUniqueId();
        Mode mode = playerMode.remove(uid);

        // If player closed without submitting and still needs to auth, reopen after 1 tick
        AuthSession session = plugin.getSessionManager().get(player);
        if (mode != null && session != null && !session.isAuthenticated()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) return;
                    AuthSession s = plugin.getSessionManager().get(player);
                    if (s != null && !s.isAuthenticated()) {
                        openFor(player);
                    }
                }
            }.runTaskLater(plugin, 5L);
        }
    }

    // -----------------------------------------------------------------------
    // Submit logic — mirrors LoginCommand / RegisterCommand / TwoFaCommand
    // -----------------------------------------------------------------------

    private void handleSubmit(Player player, Mode mode, String typed) {
        String prefix  = plugin.getGateConfig().prefix();
        String name    = player.getName();
        AuthSession session = plugin.getSessionManager().get(player);

        if (session == null || session.isAuthenticated()) return;

        switch (mode) {
            case LOGIN -> {
                String hash = plugin.getDatabase().getPasswordHash(name);
                if (hash == null) {
                    player.sendMessage(prefix + plugin.getGateConfig().notRegistered());
                    session.setState(State.WAITING_REGISTER);
                    openAnvil(player, Mode.REGISTER_PASS1);
                    return;
                }
                if (!BCrypt.checkpw(typed, hash)) {
                    session.incrementFailed();
                    int remaining = plugin.getGateConfig().maxAttempts() - session.getFailedAttempts();
                    if (remaining <= 0) {
                        player.kickPlayer(plugin.getGateConfig().maxAttemptsKick());
                        return;
                    }
                    player.sendMessage(prefix + plugin.getGateConfig().wrongPassword(remaining));
                    openAnvil(player, Mode.LOGIN);
                    return;
                }
                // Password correct — check 2FA
                String ip = session.getIp();
                boolean trusted = plugin.getIpTrustManager().isTrusted(name, ip);
                if (!trusted && plugin.getGateConfig().twoFaEnabled()
                             && plugin.getTwoFactorManager().hasPin(name)) {
                    session.setState(State.WAITING_2FA);
                    player.sendMessage(prefix + plugin.getGateConfig().newIpNotice());
                    openAnvil(player, Mode.TWO_FA);
                    return;
                }
                completeAuth(player, session);
            }

            case REGISTER_PASS1 -> {
                if (typed.length() < plugin.getGateConfig().minPasswordLength()) {
                    player.sendMessage(prefix + plugin.getGateConfig().registerTooShort());
                    openAnvil(player, Mode.REGISTER_PASS1);
                    return;
                }
                pendingPass.put(player.getUniqueId(), typed);
                player.sendMessage(prefix + "§eConfirm your password:");
                openAnvil(player, Mode.REGISTER_PASS2);
            }

            case REGISTER_PASS2 -> {
                String first = pendingPass.remove(player.getUniqueId());
                if (first == null || !first.equals(typed)) {
                    player.sendMessage(prefix + plugin.getGateConfig().registerMismatch());
                    openAnvil(player, Mode.REGISTER_PASS1);
                    return;
                }
                if (plugin.getDatabase().accountExists(name)) {
                    session.setState(State.WAITING_LOGIN);
                    player.sendMessage(prefix + plugin.getGateConfig().pleaseLogin());
                    openAnvil(player, Mode.LOGIN);
                    return;
                }
                String hash = BCrypt.hashpw(first, BCrypt.gensalt(10));
                plugin.getDatabase().createAccount(name, hash, false);
                String ip = session.getIp();
                plugin.getIpTrustManager().trust(name, ip);
                plugin.getDatabase().updateLastLogin(name, ip);
                player.sendMessage(prefix + plugin.getGateConfig().registerSuccess());
                plugin.getSessionManager().markAuthenticated(player);
                plugin.getLoginScreen().remove(player);
                plugin.getWarpTransfer().warpToLobby(player);
            }

            case TWO_FA -> {
                if (!plugin.getTwoFactorManager().hasPin(name)) {
                    player.sendMessage(prefix + "§eNo PIN set — IP auto-trusted.");
                    completeAuth(player, session);
                    return;
                }
                if (!plugin.getTwoFactorManager().verifyPin(name, typed)) {
                    session.incrementFailed();
                    int remaining = plugin.getGateConfig().maxAttempts() - session.getFailedAttempts();
                    if (remaining <= 0) {
                        player.kickPlayer(plugin.getGateConfig().maxAttemptsKick());
                        return;
                    }
                    player.sendMessage(prefix + plugin.getGateConfig().pinWrong());
                    openAnvil(player, Mode.TWO_FA);
                    return;
                }
                player.sendMessage(prefix + plugin.getGateConfig().pinCorrect());
                completeAuth(player, session);
            }
        }
    }

    private void completeAuth(Player player, AuthSession session) {
        String ip = session.getIp();
        player.sendMessage(plugin.getGateConfig().prefix() + plugin.getGateConfig().loginSuccess());
        plugin.getSessionManager().markAuthenticated(player);
        plugin.getDatabase().updateLastLogin(player.getName(), ip);
        if (!plugin.getIpTrustManager().isTrusted(player.getName(), ip)) {
            plugin.getIpTrustManager().trust(player.getName(), ip);
        }
        plugin.getLoginScreen().remove(player);
        plugin.getWarpTransfer().warpToLobby(player);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Component anvilTitle(Mode mode) {
        return switch (mode) {
            case LOGIN          -> Component.text("🔑 Login", NamedTextColor.AQUA, TextDecoration.BOLD);
            case REGISTER_PASS1 -> Component.text("✦ Create Password", NamedTextColor.GREEN, TextDecoration.BOLD);
            case REGISTER_PASS2 -> Component.text("✦ Confirm Password", NamedTextColor.GREEN, TextDecoration.BOLD);
            case TWO_FA         -> Component.text("⚿ 2FA PIN", NamedTextColor.YELLOW, TextDecoration.BOLD);
        };
    }

    private Component promptLore(Mode mode) {
        return switch (mode) {
            case LOGIN          -> Component.text("Type password → click output", NamedTextColor.GRAY);
            case REGISTER_PASS1 -> Component.text("Choose a password (min 6 chars)", NamedTextColor.GRAY);
            case REGISTER_PASS2 -> Component.text("Type the same password again", NamedTextColor.GRAY);
            case TWO_FA         -> Component.text("Type your 2FA PIN", NamedTextColor.GRAY);
        };
    }

    /** Clean up when player disconnects. */
    public void cleanup(UUID uuid) {
        playerMode.remove(uuid);
        pendingPass.remove(uuid);
    }
}
