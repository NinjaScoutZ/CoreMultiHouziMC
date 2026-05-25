package com.houzicore.shared.core.disguise.v2;

import org.bukkit.entity.LivingEntity;

import com.houzicore.shared.api.disguise.DisguiseBackend;
import com.houzicore.shared.api.disguise.DisguiseRequest;

public class NoopDisguiseBackendAdapter implements DisguiseBackendAdapter {

    @Override
    public DisguiseBackend getBackend() {
        return DisguiseBackend.NOOP;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean supports(DisguiseRequest request) {
        return true;
    }

    @Override
    public void apply(LivingEntity target, DisguiseRequest request) {
        // Intentionally empty. This backend exists as a safe fallback.
    }

    @Override
    public void clear(LivingEntity target) {
        // Intentionally empty. This backend exists as a safe fallback.
    }
}
