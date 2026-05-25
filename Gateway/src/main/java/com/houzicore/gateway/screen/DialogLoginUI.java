package com.houzicore.gateway.screen;

import com.houzicore.gateway.GatewayPlugin;
import com.houzicore.gateway.auth.AuthSession;
import com.houzicore.gateway.auth.AuthSession.State;

import io.papermc.paper.dialog.Dialog;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

/**
 * Interface coordinator for Paper 1.21.11 Dialog UI.
 * Handles the async opening/reopening of dialogs by delegating layout construction
 * to DialogFactory.
 */
@SuppressWarnings("UnstableApiUsage")
public class DialogLoginUI {

    private final GatewayPlugin plugin;
    private final DialogFactory factory;

    public DialogLoginUI(GatewayPlugin plugin) {
        this.plugin = plugin;
        this.factory = new DialogFactory(plugin);
    }

    /**
     * Open the appropriate dialog based on the player's current session state.
     * Delayed 2 ticks so the client is ready to receive the packet.
     */
    public void openFor(Player player) {
        AuthSession session = plugin.getSessionManager().get(player);
        if (session == null || session.isAuthenticated()) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;
                AuthSession s = plugin.getSessionManager().get(player);
                if (s == null || s.isAuthenticated()) return;
                doOpen(player, s.getState());
            }
        }.runTaskLater(plugin, 2L);
    }

    /** Re-open after wrong password or PIN. */
    public void reopen(Player player, String errorMessage) {
        AuthSession session = plugin.getSessionManager().get(player);
        if (session == null || session.isAuthenticated()) return;
        doOpen(player, session.getState(), errorMessage);
    }

    /** Called on player quit — no per-player state to clean. */
    public void cleanup(UUID uuid) {
        // no-op: Dialog API has no inventory session to close
    }

    private void doOpen(Player player, State state, String errorMsg) {
        Dialog dialog = buildDialog(state, errorMsg);
        if (dialog != null) {
            player.showDialog(dialog);
        }
    }

    private void doOpen(Player player, State state) {
        doOpen(player, state, null);
    }

    private Dialog buildDialog(State state, String errorMsg) {
        return switch (state) {
            case WAITING_LOGIN -> factory.buildLoginDialog(errorMsg);
            case WAITING_REGISTER -> factory.buildRegisterDialog(errorMsg);
            case WAITING_REGISTER_CONFIRM -> factory.buildRegisterConfirmDialog(errorMsg);
            case WAITING_2FA -> factory.buildTwoFaDialog(errorMsg);
            default -> null;
        };
    }
}
