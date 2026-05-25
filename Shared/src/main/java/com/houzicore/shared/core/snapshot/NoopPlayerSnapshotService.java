package com.houzicore.shared.core.snapshot;

import org.bukkit.entity.Player;

import com.houzicore.shared.api.snapshot.PlayerSnapshotService;

public class NoopPlayerSnapshotService implements PlayerSnapshotService {

    @Override
    public void capture(Player player, String snapshotId) {
        // Skeleton implementation for early wiring and migration.
    }

    @Override
    public boolean hasSnapshot(Player player, String snapshotId) {
        return false;
    }

    @Override
    public void restore(Player player, String snapshotId) {
        // Skeleton implementation for early wiring and migration.
    }

    @Override
    public void discard(Player player, String snapshotId) {
        // Skeleton implementation for early wiring and migration.
    }

    @Override
    public void cleanup(java.util.UUID playerId) {
        // No-op
    }
}
