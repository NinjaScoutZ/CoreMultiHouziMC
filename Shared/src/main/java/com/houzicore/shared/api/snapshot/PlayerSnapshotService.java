package com.houzicore.shared.api.snapshot;

import org.bukkit.entity.Player;

public interface PlayerSnapshotService {

    void capture(Player player, String snapshotId);

    boolean hasSnapshot(Player player, String snapshotId);

    void restore(Player player, String snapshotId);

    void discard(Player player, String snapshotId);

    /**
     * Best-effort memory cleanup only.
     * Removes snapshot data for offline players to prevent memory leaks.
     * @param playerId the UUID of the player who disconnected
     */
    void cleanup(java.util.UUID playerId);
}
