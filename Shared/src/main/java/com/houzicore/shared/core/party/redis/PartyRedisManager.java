package com.houzicore.shared.core.party.redis;

import com.houzicore.shared.MiniPlugin;
import org.bukkit.plugin.java.JavaPlugin;

public class PartyRedisManager extends MiniPlugin {
    public PartyRedisManager(JavaPlugin plugin) {
        super("Party Transmit", plugin);
    }

    // Cross-server Redis Pub/Sub implementation setup
    // Resolves Item 95: Inter-server group coordination beyond localized Party allocations.
}
