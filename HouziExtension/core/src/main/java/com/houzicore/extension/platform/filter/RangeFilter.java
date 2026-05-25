package com.houzicore.extension.platform.filter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.model.util.Range;
import com.houzicore.extension.platform.adapter.PlatformPlayerAdapter;
import com.houzicore.extension.util.checker.PermissionChecker;

import java.util.function.Predicate;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RangeFilter {

    private final PlatformPlayerAdapter platformPlayerAdapter;
    private final PermissionChecker permissionChecker;

    public Predicate<FPlayer> createFilter(FPlayer filterPlayer, Range range) {
        if (range.is(Range.Type.PLAYER)) {
            return filterPlayer::equals;
        }

        if (!(filterPlayer instanceof FPlayer fPlayer) || fPlayer.isUnknown()) {
            return player -> true;
        }

        return fReceiver -> {
            if (fReceiver.isUnknown()) return true;

            return switch (range.type()) {
                case BLOCKS -> checkDistance(fPlayer, fReceiver, range.value());
                case WORLD_NAME -> checkWorldNamePermission(fPlayer, fReceiver);
                case WORLD_TYPE -> checkWorldTypePermission(fPlayer, fReceiver);
                default -> true;
            };
        };
    }

    public boolean checkDistance(FPlayer fPlayer, FPlayer fReceiver, int range) {
        double distance = platformPlayerAdapter.distance(fPlayer, fReceiver);
        return distance != -1.0 && distance <= range;
    }

    public boolean checkWorldNamePermission(FPlayer fPlayer, FPlayer fReceiver) {
        String worldName = platformPlayerAdapter.getWorldName(fPlayer);
        if (worldName.isEmpty()) return true;
        return permissionChecker.check(fReceiver, "houziextension.world.name." + worldName);
    }

    public boolean checkWorldTypePermission(FPlayer fPlayer, FPlayer fReceiver) {
        String worldType = platformPlayerAdapter.getWorldEnvironment(fPlayer);
        if (worldType.isEmpty()) return true;
        return permissionChecker.check(fReceiver, "houziextension.world.type." + worldType);
    }

}
