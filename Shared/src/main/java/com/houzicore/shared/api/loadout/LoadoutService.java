package com.houzicore.shared.api.loadout;

import org.bukkit.entity.Player;

public interface LoadoutService {

    void apply(Player player, LoadoutProfile profile);

    void clear(Player player);
}
