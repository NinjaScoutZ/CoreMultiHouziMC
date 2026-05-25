package com.houzicore.extension.data.repository;

import com.google.common.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.execution.scheduler.TaskScheduler;
import com.houzicore.extension.model.entity.FPlayer;
import com.houzicore.extension.model.util.Cooldown;
import com.houzicore.extension.platform.registry.ProxyRegistry;
import com.houzicore.extension.platform.sender.ProxySender;
import com.houzicore.extension.util.constant.ModuleName;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CooldownRepository {

    private final @Named("cooldown") Cache<CooldownKey, Long> cooldownCache;
    private final ProxyRegistry proxyRegistry;
    private final ProxySender proxySender;
    private final TaskScheduler taskScheduler;

    public long getTimeLeft(UUID playerUUID, Cooldown cooldown, String cooldownOwner) {
        if (cooldown == null || !cooldown.enable()) return 0;

        CooldownKey cooldownKey = new CooldownKey(playerUUID, cooldownOwner);

        Long expireTime = cooldownCache.getIfPresent(cooldownKey);
        if (expireTime == null) return 0;

        return Math.max(0, expireTime - System.currentTimeMillis());
    }

    public boolean updateCache(UUID playerUUID, String cooldownClass, long newExpireTime) {
        CooldownKey cooldownKey = new CooldownKey(playerUUID, cooldownClass);

        Long expireTime = cooldownCache.getIfPresent(cooldownKey);
        if (expireTime == null || expireTime < System.currentTimeMillis()) {
            // do not update time if it is less than the current one
            if (expireTime != null && newExpireTime < expireTime) return true;

            cooldownCache.put(cooldownKey, newExpireTime);
            return true;
        }

        return false;
    }

    public void syncProxy(UUID playerUUID, String cooldownOwner, long newExpireTime) {
        taskScheduler.runAsync(() -> {
            if (proxyRegistry.hasEnabledProxy()) {
                proxySender.send(FPlayer.UNKNOWN, ModuleName.SYSTEM_COOLDOWN, dataOutputStream -> {
                    dataOutputStream.writeUTF(playerUUID.toString());
                    dataOutputStream.writeUTF(cooldownOwner);
                    dataOutputStream.writeLong(newExpireTime);
                }, UUID.randomUUID());
            }
        });
    }

    public record CooldownKey(UUID playerUUID, String cooldownClass) {
    }

}
