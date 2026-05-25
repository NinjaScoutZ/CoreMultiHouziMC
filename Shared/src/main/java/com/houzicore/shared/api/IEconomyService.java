package com.houzicore.shared.api;

import org.bukkit.entity.Player;

/**
 * Public API contract for economy operations.
 * Arcade/Lobby should depend on this interface, not DonationManager directly.
 */
public interface IEconomyService {
    int getCoins(Player player);
    int getEssence(Player player);
    void addCoins(Player player, String reason, int amount);
    void addEssence(Player player, String reason, int amount);
    boolean hasCoins(Player player, int amount);
}
