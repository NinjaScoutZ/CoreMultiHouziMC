package com.houzicore.extension.util.checker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import com.houzicore.extension.data.repository.CooldownRepository;
import com.houzicore.extension.model.util.Cooldown;
import com.houzicore.extension.platform.formatter.TimeFormatter;

import java.util.UUID;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CooldownChecker {

    private final CooldownRepository cooldownRepository;

    public boolean check(UUID playerUUID, Cooldown cooldown, @NonNull String cooldownOwner) {
        if (cooldown == null || !cooldown.enable()) return false;

        long currentTimeMillis = System.currentTimeMillis();
        long newExpireTime = currentTimeMillis + cooldown.duration() * TimeFormatter.MULTIPLIER;
        if (cooldownRepository.updateCache(playerUUID, cooldownOwner, newExpireTime)) {
            cooldownRepository.syncProxy(playerUUID, cooldownOwner, newExpireTime);
            return false;
        }

        return true;
    }


}
