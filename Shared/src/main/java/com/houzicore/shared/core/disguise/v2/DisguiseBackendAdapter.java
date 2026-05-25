package com.houzicore.shared.core.disguise.v2;

import org.bukkit.entity.LivingEntity;

import com.houzicore.shared.api.disguise.DisguiseBackend;
import com.houzicore.shared.api.disguise.DisguiseRequest;

public interface DisguiseBackendAdapter {

    DisguiseBackend getBackend();

    boolean isAvailable();

    boolean supports(DisguiseRequest request);

    void apply(LivingEntity target, DisguiseRequest request);

    void clear(LivingEntity target);
}
