package com.houzicore.arcade.nautilus.game.arcade.managers;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.houzicore.shared.common.util.C;
import com.houzicore.shared.common.util.F;
import com.houzicore.shared.common.util.UtilServer;
import com.houzicore.shared.common.actionbar.ActionBarChannel;
import com.houzicore.shared.common.actionbar.ActionBarService;
import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.nautilus.game.arcade.events.GameStateChangeEvent;
import com.houzicore.arcade.nautilus.game.arcade.game.Game.GameState;

/**
 * Manages per-round in-game coins — ephemeral currency that resets each game.
 * Players earn coins by kills/assists. Coins can be used to buy power-ups mid-game.
 */
public class GameCoinManager implements org.bukkit.event.Listener {

    private final ArcadeManager Manager;
    // Per-round coin balance (reset on game end)
    private final HashMap<UUID, Integer> _coins = new HashMap<>();

    public GameCoinManager(ArcadeManager manager) {
        Manager = manager;
        Manager.getPluginManager().registerEvents(this, Manager.getPlugin());
    }

    /** Returns the current in-game coin balance for a player. */
    public int getCoins(Player player) {
        return _coins.getOrDefault(player.getUniqueId(), 0);
    }

    /** Adds coins to a player's balance and sends a notification. */
    public void addCoins(Player player, int amount, String reason) {
        int current = getCoins(player);
        _coins.put(player.getUniqueId(), current + amount);
        ActionBarService.display(player, ActionBarChannel.REWARD, net.kyori.adventure.text.Component.text(
            "+" + amount + " 🪙 (" + reason + ")",
            net.kyori.adventure.text.format.NamedTextColor.YELLOW
        ));
    }

    /** Deducts coins from a player. Returns true if successful. */
    public boolean spendCoins(Player player, int amount) {
        if (getCoins(player) < amount) return false;
        _coins.put(player.getUniqueId(), getCoins(player) - amount);
        return true;
    }

    /** Resets all coin balances at game end. */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameEnd(GameStateChangeEvent event) {
        if (event.GetState() == GameState.Dead || event.GetState() == GameState.End) {
            _coins.clear();
        }
    }

    /** XP award integration: award coins and XP on kill to all listeners. */
    public void awardKillReward(Player killer, Player victim) {
        if (killer == null || !killer.isOnline()) return;
        addCoins(killer, 10, "Kill");

        // Award XP via LvlManager if available
        if (Manager.getLvlManager() != null) {
            Manager.getLvlManager().awardXp(killer, 50, "Kill");
        }
    }
}
